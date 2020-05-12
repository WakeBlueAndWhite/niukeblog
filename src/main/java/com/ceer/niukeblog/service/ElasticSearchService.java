package com.ceer.niukeblog.service;

import com.ceer.niukeblog.entity.DiscussPost;
import org.springframework.data.domain.Page;

/**
 *@ClassName ElasticSearchService
 *@Description TODO
 *@Author ceer
 *@Date 2020/5/7 22:37
 *@Version 1.0
 */
public interface ElasticSearchService {

    void saveDiscussPost(DiscussPost post);

    void deleteDiscussPost(Integer id);

    Page<DiscussPost> searchDiscussPost(String keyword, Integer current, Integer limit);
}
