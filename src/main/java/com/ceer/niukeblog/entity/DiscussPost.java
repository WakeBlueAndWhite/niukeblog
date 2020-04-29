package com.ceer.niukeblog.entity;

import java.util.Date;
import lombok.Data;
/**
 *@ClassName DiscussPost
 *@Description
 *@Author ceer
 *@Date 2020/4/29 14:31
 *@Version 1.0
 */
@Data
public class DiscussPost {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 0-普通; 1-置顶;
     */
    private Integer type;

    /**
     * 0-正常; 1-精华; 2-拉黑;
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 文章数量
     */
    private Integer commentCount;

    /**
     * 分数
     */
    private Double score;
}