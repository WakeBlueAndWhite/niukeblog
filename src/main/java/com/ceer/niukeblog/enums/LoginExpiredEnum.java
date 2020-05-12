package com.ceer.niukeblog.enums;

/**
 *@ClassName LoginExpiredEnum
 *@Description 登录凭证枚举类
 *@Author ceer
 *@Date 2020/4/30 22:26
 *@Version 1.0
 */
public enum LoginExpiredEnum {
    /**
     * 默认状态的登录凭证的超时时间
     */
    DEFAULT_EXPIRED_SECONDS(3600 * 12),
    /**
     * 记住我的登录凭证超时时间
     */
    REMEMBER_EXPIRED_SECONDS(3600 * 24 * 30);
    ;

    private int time;

    LoginExpiredEnum(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
