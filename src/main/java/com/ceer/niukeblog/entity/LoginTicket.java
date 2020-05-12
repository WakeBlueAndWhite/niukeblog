package com.ceer.niukeblog.entity;

import java.util.Date;
import lombok.Data;
/**
 *@ClassName LoginTicket
 *@Description 登录凭证
 *@Author ceer
 *@Date 2020/4/30 19:57
 *@Version 1.0
 */
@Data
public class LoginTicket {
    /**
    * 主键
    */
    private Integer id;

    /**
    * 用户id
    */
    private Integer userId;

    /**
    * 登录凭证
    */
    private String ticket;

    /**
    * 0-有效; 1-无效;
    */
    private Integer status;

    /**
    * 失效时间
    */
    private Date expired;
}