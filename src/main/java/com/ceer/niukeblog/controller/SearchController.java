package com.ceer.niukeblog.controller;

import com.ceer.niukeblog.entity.DiscussPost;
import com.ceer.niukeblog.entity.Page;
import com.ceer.niukeblog.enums.CommentEnum;
import com.ceer.niukeblog.service.ElasticSearchService;
import com.ceer.niukeblog.service.LikeService;
import com.ceer.niukeblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SearchController
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/7 23:18
 * @Version 1.0
 */
@Controller
public class SearchController {

    @Autowired
    private ElasticSearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        // 搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.selectByPrimaryKey(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(CommentEnum.ENTITY_TYPE_POST.getType(), post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());

        return "/site/search";
    }
}
