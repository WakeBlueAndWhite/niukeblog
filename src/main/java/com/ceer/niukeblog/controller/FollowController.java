package com.ceer.niukeblog.controller;

import com.ceer.niukeblog.config.RabbitMQConfig;
import com.ceer.niukeblog.entity.Page;
import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.enums.FollowEnum;
import com.ceer.niukeblog.rabbitmq.Event;
import com.ceer.niukeblog.rabbitmq.MQSender;
import com.ceer.niukeblog.service.FollowService;
import com.ceer.niukeblog.service.UserService;
import com.ceer.niukeblog.util.CommunityUtil;
import com.ceer.niukeblog.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName FollowController
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/4 22:14
 * @Version 1.0
 */
@Controller
public class FollowController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private MQSender mqSender;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(Integer entityType,Integer entityId){
        User user = hostHolder.getUser();
        //关注
        followService.follow(user.getId(),entityType,entityId);
        // 触发关注事件
        Event event = new Event()
                .setTopic(RabbitMQConfig.FOLLOW_KEY)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        mqSender.sendMessage(event,event.getTopic(),RabbitMQConfig.DirectExchangeName);
        return CommunityUtil.getJSONString(0, "已关注!");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(Integer entityType,Integer entityId){
        User user = hostHolder.getUser();
        //取消关注
        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0, "已取消关注!");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") Integer userId, Page page, Model model) {
        User user = userService.selectByPrimaryKey(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！！！");
        }
        model.addAttribute("loginUser",hostHolder.getUser());
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, FollowEnum.FOLLOW_USER.getType()));

        List<Map<String, Object>> followeesList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        for (Map<String,Object> map : followeesList){
            User u = (User) map.get("user");
            map.put("hasFollowed", hasFollowed(u.getId()));
        }
        model.addAttribute("users", followeesList);

        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") Integer userId, Page page, Model model) {
        User user = userService.selectByPrimaryKey(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！！！");
        }
        model.addAttribute("loginUser",hostHolder.getUser());
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(FollowEnum.FOLLOW_USER.getType(),userId));

        List<Map<String, Object>> followersList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        for (Map<String,Object> map : followersList){
            User u = (User) map.get("user");
            map.put("hasFollowed", hasFollowed(u.getId()));
        }
        model.addAttribute("users", followersList);

        return "/site/follower";
    }
    /**
     * @Description: 验证是否关注
     * @param:
     * @return:
     * @date: 2020/5/5 15:17
     */
    private boolean hasFollowed(Integer userId) {
        if (userId == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),FollowEnum.FOLLOW_USER.getType(),userId);
    }
}
