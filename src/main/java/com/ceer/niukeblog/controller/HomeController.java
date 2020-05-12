package com.ceer.niukeblog.controller;

import com.ceer.niukeblog.entity.DiscussPost;
import com.ceer.niukeblog.entity.Page;
import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.enums.CommentEnum;
import com.ceer.niukeblog.service.DiscussPostService;
import com.ceer.niukeblog.service.FollowService;
import com.ceer.niukeblog.service.LikeService;
import com.ceer.niukeblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName HomeController
 * @Description TODO
 * @Author ceer
 * @Date 2020/4/29 16:00
 * @Version 1.0
 */
@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") Integer orderMode){
        // 方法调用钱,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        //设置文章总数index
        page.setRows(discussPostService.findDiscussPostRows(0));
        //设置访问路径
        page.setPath("/index?orderMode=" + orderMode);
        //获取文章
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        List<Map<String,Object>> discussPostsList = new ArrayList<>();
        for (DiscussPost  post : discussPosts) {
            //将每个用户对应的文章存入map中
            Map<String,Object> map = new HashMap<>();
            map.put("post", post);
            User user = userService.selectByPrimaryKey(post.getUserId());
            map.put("user",user);
            //查询帖子的点赞数量
            long likeCount = likeService.findEntityLikeCount(CommentEnum.ENTITY_TYPE_POST.getType(), post.getId());
            map.put("likeCount", likeCount);
            discussPostsList.add(map);
        }
        model.addAttribute("discussPostsList", discussPostsList);
        model.addAttribute("orderMode", orderMode);
        return "/index";
    }


    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }

}
