package com.ceer.niukeblog.entity;

import java.util.Date;
import lombok.Data;

@Data
public class Comment {
    /**
    * 主键
    */
    private Integer id;

    /**
    * 用户id
    */
    private Integer userId;

    /**
    * 实体类型 帖子or评论or...
    */
    private Integer entityType;

    /**
    * 实体id
    */
    private Integer entityId;

    /**
    * 目标id
    */
    private Integer targetId;

    /**
    * 评论
    */
    private String content;

    /**
    * 状态 0-正常 1-被禁止
    */
    private Integer status;

    /**
    * 创建时间
    */
    private Date createTime;
}