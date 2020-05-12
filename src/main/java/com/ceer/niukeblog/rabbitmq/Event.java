package com.ceer.niukeblog.rabbitmq;

import java.util.HashMap;
import java.util.Map;

public class Event {

    private String topic;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 实体类型 评论 点赞 关注
     */
    private Integer entityType;
    /**
     * 实体id
     */
    private Integer entityId;
    /**
     * 实体用户id
     */
    private Integer entityUserId;
    private Map<String, Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Integer getUserId() {
        return userId;
    }

    public Event setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public Integer getEntityType() {
        return entityType;
    }

    public Event setEntityType(Integer entityType) {
        this.entityType = entityType;
        return this;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public Event setEntityId(Integer entityId) {
        this.entityId = entityId;
        return this;
    }

    public Integer getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(Integer entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

}
