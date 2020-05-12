package com.ceer.niukeblog.service;

import com.ceer.niukeblog.entity.LoginTicket;
import com.ceer.niukeblog.entity.User;

import java.util.Map;

public interface UserService{

    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    Map<String, Object> register(User user);

    int activation(Integer userId, String code);

    Map<String, Object> login(String username, String password, int expiredSeconds);

    void logout(String ticket);

    LoginTicket findLoginTicket(String ticket);

    void updateHeader(Integer id, String headerUrl);

    Map<String,Object> checkPassword(String oldPassword,User user);

    int updatePassword(String password,Integer id);

    User findUserByName(String username);

   // Collection<? extends GrantedAuthority> getAuthorities(Integer userId);
}
