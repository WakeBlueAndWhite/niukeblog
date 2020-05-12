package com.ceer.niukeblog.entity;

import java.util.Date;
import lombok.Data;

@Data
public class Message {
    /**
    * 主键
    */
    private Integer id;

    /**
    * 谁发送的消息--其用户id
    */
    private Integer fromId;

    /**
    * 谁接受的消息--其用户id
    */
    private Integer toId;

    /**
    * 会话id from和to拼接
    */
    private String conversationId;

    /**
    * 发送的消息
    */
    private String content;

    /**
    * 0-未读;1-已读;2-删除;
    */
    private Integer status;

    /**
    * 创建时间
    */
    private Date createTime;
}