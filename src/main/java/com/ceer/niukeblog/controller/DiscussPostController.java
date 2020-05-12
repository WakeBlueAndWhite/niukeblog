package com.ceer.niukeblog.controller;

import com.ceer.niukeblog.config.RabbitMQConfig;
import com.ceer.niukeblog.entity.Comment;
import com.ceer.niukeblog.entity.DiscussPost;
import com.ceer.niukeblog.entity.Page;
import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.enums.CommentEnum;
import com.ceer.niukeblog.rabbitmq.Event;
import com.ceer.niukeblog.rabbitmq.MQSender;
import com.ceer.niukeblog.service.CommentService;
import com.ceer.niukeblog.service.DiscussPostService;
import com.ceer.niukeblog.service.LikeService;
import com.ceer.niukeblog.service.UserService;
import com.ceer.niukeblog.util.CommunityUtil;
import com.ceer.niukeblog.util.HostHolder;
import com.ceer.niukeblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @ClassName DiscussPostController
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/1 23:36
 * @Version 1.0
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403,"您还没有登录！");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setStatus(0);
        discussPost.setType(0);
        discussPost.setCommentCount(0);
        discussPost.setScore(7.7);
        discussPost.setCreateTime(new Date());
        discussPostService.insert(discussPost);

        // 触发发帖事件 将帖子存入es中
        Event event = new Event()
                .setTopic(RabbitMQConfig.PUBLISH_KEY)
                .setUserId(user.getId() )
                .setEntityType(CommentEnum.ENTITY_TYPE_POST.getType())
                .setEntityId(discussPost.getId());
        mqSender.sendMessage(event, event.getTopic(), RabbitMQConfig.DirectExchangeName);
        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());
        return CommunityUtil.getJSONString(0,"发布成功！！！");
    }

    @GetMapping("/detail/{id}")
    public String getDiscussPost(@PathVariable("id")Integer id, Model model, Page page){
        //帖子
        DiscussPost discussPost = discussPostService.selectByPrimaryKey(id);
        model.addAttribute("post",discussPost);
        //作者
        User user = userService.selectByPrimaryKey(discussPost.getUserId());
        model.addAttribute("user",user);
        // 帖子的点赞数量
        long likeCount = likeService.findEntityLikeCount(CommentEnum.ENTITY_TYPE_POST.getType(), id);
        model.addAttribute("likeCount", likeCount);
        // 当前登录用户对帖子的点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), CommentEnum.ENTITY_TYPE_POST.getType(), id);
        model.addAttribute("likeStatus", likeStatus);
        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + id);
        page.setRows(discussPost.getCommentCount());
        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                CommentEnum.ENTITY_TYPE_POST.getType(), discussPost.getId(), page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.selectByPrimaryKey(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(CommentEnum.ENTITY_TYPE_COMMENT.getType(),comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(),
                                    CommentEnum.ENTITY_TYPE_COMMENT.getType(), comment.getId());
                commentVo.put("likeStatus", likeStatus);
                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        CommentEnum.ENTITY_TYPE_COMMENT.getType(), comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.selectByPrimaryKey(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.selectByPrimaryKey(reply.getTargetId());
                        replyVo.put("target", target);
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(CommentEnum.ENTITY_TYPE_COMMENT.getType(), reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),
                                        CommentEnum.ENTITY_TYPE_COMMENT.getType(), reply.getId());
                        replyVo.put("likeStatus", likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 评论数量
                int replyCount = commentService.findCommentCount(CommentEnum.ENTITY_TYPE_COMMENT.getType(), comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }

    /**
     * @Description: 置顶
     * @param:
     * @return:
     * @date: 2020/5/8 23:00
     */
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(Integer id) {
        discussPostService.updateType(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(RabbitMQConfig.PUBLISH_KEY)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommentEnum.ENTITY_TYPE_POST.getType())
                .setEntityId(id);
        //更改为置顶后更新es中的数据
        mqSender.sendMessage(event,RabbitMQConfig.PUBLISH_KEY,RabbitMQConfig.DirectExchangeName);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * @Description: 加精
     * @param:
     * @return:
     * @date: 2020/5/8 23:01
     */
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(Integer id) {
        discussPostService.updateStatus(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(RabbitMQConfig.PUBLISH_KEY)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommentEnum.ENTITY_TYPE_POST.getType())
                .setEntityId(id);
        //更改为加精后更新es中的数据
        mqSender.sendMessage(event,RabbitMQConfig.PUBLISH_KEY,RabbitMQConfig.DirectExchangeName);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * @Description: 删除
     * @param:
     * @return:
     * @date: 2020/5/8 23:01
     */
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(Integer id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件
        Event event = new Event()
                .setTopic(RabbitMQConfig.DELETE_KEY)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(CommentEnum.ENTITY_TYPE_POST.getType())
                .setEntityId(id);
        mqSender.sendMessage(event,RabbitMQConfig.DELETE_KEY,RabbitMQConfig.DirectExchangeName);

        return CommunityUtil.getJSONString(0);
    }

}
