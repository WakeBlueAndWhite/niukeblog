package com.ceer.niukeblog.entity;

import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 *@ClassName DiscussPost
 *@Description
 *@Author ceer
 *@Date 2020/4/29 14:31
 *@Version 1.0
 */
@Data
@Document(indexName = "discusspost", type = "_doc", shards = 6, replicas = 3)
public class DiscussPost {
    /**
     * 主键
     */
    @Id
    private Integer id;

    /**
     * 用户id
     */
    @Field(type = FieldType.Integer)
    private Integer userId;

    /**
     * 标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 文章内容
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 0-普通; 1-置顶;
     */
    @Field(type = FieldType.Integer)
    private Integer type;

    /**
     * 0-正常; 1-精华; 2-拉黑;
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date)
    private Date createTime;

    /**
     * 文章数量
     */
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    /**
     * 分数
     */
    @Field(type = FieldType.Double)
    private Double score;
}