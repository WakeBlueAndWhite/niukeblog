package com.ceer.niukeblog.mapper;

import com.ceer.niukeblog.entity.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Comment record);

    int insertSelective(Comment record);

    Comment selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Comment record);

    int updateByPrimaryKey(Comment record);

    List<Comment> selectCommentsByEntity(@Param("entityType") Integer entityType,
                                         @Param("entityId") Integer entityId,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

    int selectCountByEntity(@Param("entityType") Integer entityType,
                            @Param("entityId") Integer entityId);

}