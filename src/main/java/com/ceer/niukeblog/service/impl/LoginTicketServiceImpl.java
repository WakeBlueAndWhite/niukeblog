package com.ceer.niukeblog.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.ceer.niukeblog.entity.LoginTicket;
import com.ceer.niukeblog.mapper.LoginTicketMapper;
import com.ceer.niukeblog.service.LoginTicketService;
@Service
public class LoginTicketServiceImpl implements LoginTicketService{

    @Resource
    private LoginTicketMapper loginTicketMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return loginTicketMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(LoginTicket record) {
        return loginTicketMapper.insert(record);
    }

    @Override
    public int insertSelective(LoginTicket record) {
        return loginTicketMapper.insertSelective(record);
    }

    @Override
    public LoginTicket selectByPrimaryKey(Integer id) {
        return loginTicketMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(LoginTicket record) {
        return loginTicketMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(LoginTicket record) {
        return loginTicketMapper.updateByPrimaryKey(record);
    }

}
