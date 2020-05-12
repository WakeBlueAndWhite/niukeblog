package com.ceer.niukeblog.mapper;

import com.ceer.niukeblog.entity.LoginTicket;
import org.apache.ibatis.annotations.Param;

public interface LoginTicketMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(LoginTicket record);

    int insertSelective(LoginTicket record);

    LoginTicket selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LoginTicket record);

    int updateByPrimaryKey(LoginTicket record);

    LoginTicket selectByTicket(String ticket);

    int updateStatus(@Param("status") Integer status, @Param("ticket") String ticket);
}