package com.ceer.niukeblog.enums;

/**
 *@ClassName FollowEnum
 *@Description TODO
 *@Author ceer
 *@Date 2020/5/4 22:12
 *@Version 1.0
 */
public enum FollowEnum {
    /**
     * 关注的是用户
     */
    FOLLOW_USER(3),
    ;

    private Integer type;

    FollowEnum(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
