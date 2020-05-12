package com.ceer.niukeblog.elasticsearch;

import com.ceer.niukeblog.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 *@ClassName DiscussPostRepository
 *@Description TODO
 *@Author ceer
 *@Date 2020/5/7 21:44
 *@Version 1.0
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
}
