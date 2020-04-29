package com.ceer.niukeblog.controller;

import com.ceer.niukeblog.entity.DiscussPost;
import com.ceer.niukeblog.entity.Page;
import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.service.DiscussPostService;
import com.ceer.niukeblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping("/index")
    public String getIndexPage(Model model, Page page){
        // 方法调用钱,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        //设置文章总数index
        page.setRows(discussPostService.findDiscussPostRows(0));
        //设置访问路径
        page.setPath("/index");
        //获取文章
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPostsList = new ArrayList<>();
        for (DiscussPost  post : discussPosts) {
            //将每个用户对应的文章存入map中
            Map<String,Object> map = new HashMap<>();
            map.put("post", post);
            User user = userService.selectByPrimaryKey(post.getUserId());
            map.put("user",user);
            discussPostsList.add(map);
        }
        model.addAttribute("discussPostsList", discussPostsList);
        return "/index";
    }
}
