package com.ceer.niukeblog.controller;

import com.ceer.niukeblog.config.RabbitMQConfig;
import com.ceer.niukeblog.entity.Comment;
import com.ceer.niukeblog.entity.DiscussPost;
import com.ceer.niukeblog.enums.CommentEnum;
import com.ceer.niukeblog.rabbitmq.Event;
import com.ceer.niukeblog.rabbitmq.MQSender;
import com.ceer.niukeblog.service.CommentService;
import com.ceer.niukeblog.service.DiscussPostService;
import com.ceer.niukeblog.util.HostHolder;
import com.ceer.niukeblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @ClassName CommentController
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/2 20:18
 * @Version 1.0
 */
@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add/{id}", method = RequestMethod.POST)
    public String addComment(@PathVariable("id") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        if (comment.getTargetId() == null) {
            comment.setTargetId(0);
        }
        commentService.insert(comment);
        // 触发评论事件
        Event event = new Event()
                .setTopic(RabbitMQConfig.COMMENT_KEY)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        //评论的是帖子
        if (comment.getEntityType() == CommentEnum.ENTITY_TYPE_POST.getType()) {
            DiscussPost target = discussPostService.selectByPrimaryKey(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
            //评论的是回复
        } else if (comment.getEntityType() == CommentEnum.ENTITY_TYPE_COMMENT.getType()) {
            Comment target = commentService.selectByPrimaryKey(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        //发送消息
        mqSender.sendMessage(event, event.getTopic(), RabbitMQConfig.DirectExchangeName);

        if (comment.getEntityType() == CommentEnum.ENTITY_TYPE_POST.getType()) {
            // 触发发帖事件 将帖子存入es中
            event = new Event()
                    .setTopic(RabbitMQConfig.PUBLISH_KEY)
                    .setUserId(comment.getUserId())
                    .setEntityType(CommentEnum.ENTITY_TYPE_POST.getType())
                    .setEntityId(discussPostId);
            mqSender.sendMessage(event, event.getTopic(), RabbitMQConfig.DirectExchangeName);
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
