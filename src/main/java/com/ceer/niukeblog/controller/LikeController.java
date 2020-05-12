package com.ceer.niukeblog.controller;

import com.ceer.niukeblog.config.RabbitMQConfig;
import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.enums.CommentEnum;
import com.ceer.niukeblog.rabbitmq.Event;
import com.ceer.niukeblog.rabbitmq.MQSender;
import com.ceer.niukeblog.service.LikeService;
import com.ceer.niukeblog.util.CommunityUtil;
import com.ceer.niukeblog.util.HostHolder;
import com.ceer.niukeblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName LikeController
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/4 15:56
 * @Version 1.0
 */
@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(Integer entityType, Integer entityId, Integer entityUserId,Integer postId) {
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(RabbitMQConfig.LIKE_KEY)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
          mqSender.sendMessage(event,event.getTopic(),RabbitMQConfig.DirectExchangeName);
        }
        if(entityType == CommentEnum.ENTITY_TYPE_POST.getType()) {
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }
        return CommunityUtil.getJSONString(0, null, map);
    }
}
