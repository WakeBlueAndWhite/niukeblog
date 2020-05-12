package com.ceer.niukeblog.service;

import com.ceer.niukeblog.entity.LoginTicket;
public interface LoginTicketService{

    int deleteByPrimaryKey(Integer id);

    int insert(LoginTicket record);

    int insertSelective(LoginTicket record);

    LoginTicket selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LoginTicket record);

    int updateByPrimaryKey(LoginTicket record);

}
