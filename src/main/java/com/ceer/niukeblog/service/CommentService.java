package com.ceer.niukeblog.service;

import com.ceer.niukeblog.entity.Comment;

import java.util.List;

public interface CommentService{


    int deleteByPrimaryKey(Integer id);

    int insert(Comment comment);

    int insertSelective(Comment record);

    Comment selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Comment record);

    int updateByPrimaryKey(Comment record);

    List<Comment> findCommentsByEntity(Integer entityType, Integer entityId, Integer offset, Integer limit);

    int findCommentCount(Integer entityType, Integer entityId);

    //int findReplyCount(Integer entityType);
}
