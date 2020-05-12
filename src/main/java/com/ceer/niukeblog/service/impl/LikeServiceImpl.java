package com.ceer.niukeblog.service.impl;

import com.ceer.niukeblog.service.LikeService;
import com.ceer.niukeblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @ClassName LikeServiceImpl
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/4 15:39
 * @Version 1.0
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @Description: 点赞
     * @param:
     * @return:
     * @date: 2020/5/4 15:42
     */
    @Override
    public void like(Integer userId, Integer entityType, Integer entityId, Integer entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //根据key查看集合中是否存在指定数据
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                //开启事务
                operations.multi();
                //如果存在表示已经点赞 再次点击取消点赞 移除对应的值 并将用户的赞减少1
                if (isMember) {
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    //否则表示没有点赞 点击表示点赞 存贮对应的值 并将用户的赞增加1
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });
    }

    /**
     * @Description: 查询某实体点赞的数量
     * @param:
     * @return:
     * @date: 2020/5/4 15:52
     */
    @Override
    public long findEntityLikeCount(Integer entityType, Integer entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * @Description: 查询某人对某实体的点赞状态
     * @param:
     * @return:
     * @date: 2020/5/4 15:52
     */
    @Override
    public Integer findEntityLikeStatus(Integer userId, Integer entityType, Integer entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    /**
     * @Description: 查询某个用户获得的赞
     * @param:
     * @return:
     * @date: 2020/5/4 15:54
     */
    @Override
    public Integer findUserLikeCount(Integer userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }



}
