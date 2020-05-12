package com.ceer.niukeblog.service.impl;

import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.enums.FollowEnum;
import com.ceer.niukeblog.service.FollowService;
import com.ceer.niukeblog.service.UserService;
import com.ceer.niukeblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName FollowServiceImpl
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/4 21:31
 * @Version 1.0
 */
@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * @Description: 关注
     * @param:
     * @return:
     * @date: 2020/5/4 21:33
     */
    @Override
    public void follow(Integer userId, Integer entityType, Integer entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                //开启事务
                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    @Override
    public void unfollow(Integer userId, Integer entityType, Integer entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }

    /**
     * @Description: 查询用户关注的实体的数量
     * @param:
     * @return:
     * @date: 2020/5/4 22:05
     */
    @Override
    public long findFolloweeCount(Integer userId, Integer entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * @Description: 查询实体的粉丝的数量
     * @param:
     * @return:
     * @date: 2020/5/4 22:06
     */
    @Override
    public long findFollowerCount(Integer entityType, Integer userId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, userId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * @Description: 查询当前用户是否已关注该实体
     * @param:
     * @return:
     * @date: 2020/5/4 22:07
     */
    @Override
    public boolean hasFollowed(Integer userId, Integer entityType, Integer entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     * @Description: 查询用户关注的粉丝
     * @param:
     * @return:
     * @date: 2020/5/5 14:55
     */
    @Override
    public List<Map<String, Object>> findFollowees(Integer userId, Integer offset, Integer limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, FollowEnum.FOLLOW_USER.getType());
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.selectByPrimaryKey(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * @Description: 查询某用户的粉丝
     * @param:
     * @return:
     * @date: 2020/5/5 15:02
     */
    @Override
    public List<Map<String, Object>> findFollowers(Integer userId, Integer offset, Integer limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(FollowEnum.FOLLOW_USER.getType(), userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.selectByPrimaryKey(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            mapList.add(map);
        }
        return mapList;
    }
}
