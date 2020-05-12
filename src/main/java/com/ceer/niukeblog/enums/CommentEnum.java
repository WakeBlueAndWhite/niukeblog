package com.ceer.niukeblog.enums;

import lombok.Getter;
import lombok.Setter;

/**
 *@ClassName CommentEnum
 *@Description TODO
 *@Author ceer
 *@Date 2020/5/2 17:03
 *@Version 1.0
 */
public enum CommentEnum {
    /**
     * 实体类型: 帖子
     */
    ENTITY_TYPE_POST(1),
    /**
     * 实体类型: 评论or回复
     */
    ENTITY_TYPE_COMMENT(2)
    ;

    private Integer type;

    CommentEnum(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
