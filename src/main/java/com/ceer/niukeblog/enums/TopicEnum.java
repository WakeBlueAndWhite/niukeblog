package com.ceer.niukeblog.enums;

/**
 *@ClassName TopicEnum
 *@Description TODO
 *@Author ceer
 *@Date 2020/5/8 23:11
 *@Version 1.0
 */
public enum TopicEnum {
    /**
     * 主题: 删帖
     */
    TOPIC_DELETE("delete");
    ;

    private String topic;

    TopicEnum(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
