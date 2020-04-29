package com.ceer.niukeblog.entity;

import java.util.Date;
import lombok.Data;
/**
 *@ClassName User
 *@Description 用户实体类
 *@Author ceer
 *@Date 2020/4/29 14:30
 *@Version 1.0
 */
@Data
public class User {

    /**
     * 主键 用户id
     */
    private Integer id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 盐
     */
    private String salt;
    /**
     * 邮箱
     */
    private String email;

    /**
    * 0-普通用户; 1-超级管理员; 2-版主;
    */
    private Integer type;

    /**
    * 0-未激活; 1-已激活;
    */
    private Integer status;

    private String activationCode;
    /**
     * 用户头像
     */
    private String headerUrl;
    /**
     * 创建时间
     */
    private Date createTime;
}