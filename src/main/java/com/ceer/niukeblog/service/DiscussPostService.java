package com.ceer.niukeblog.service;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import com.ceer.niukeblog.mapper.DiscussPostMapper;
import com.ceer.niukeblog.entity.DiscussPost;

import java.util.List;

@Service
public interface DiscussPostService {

    int deleteByPrimaryKey(Integer id);


    int insert(DiscussPost record);


    int insertSelective(DiscussPost record);


    DiscussPost selectByPrimaryKey(Integer id);


    int updateByPrimaryKeySelective(DiscussPost record);


    int updateByPrimaryKey(DiscussPost record);

    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit);

    int findDiscussPostRows(int userId);
}

