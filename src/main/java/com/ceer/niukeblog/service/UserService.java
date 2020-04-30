package com.ceer.niukeblog.service;

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
}
