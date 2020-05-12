package com.ceer.niukeblog.enums;
/**
 *@ClassName SecurityEnum
 *@Description TODO
 *@Author ceer
 *@Date 2020/5/8 19:22
 *@Version 1.0
 */
public enum SecurityEnum {
    /**
     * 权限: 普通用户
     */
    AUTHORITY_USER("user"),
    /**
     * 权限: 管理员
     */
    AUTHORITY_ADMIN("admin"),
    /**
     * 权限: 版主
     */
    AUTHORITY_MODERATOR("moderator")
    ;

    private String role;

    SecurityEnum(String role) {
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
