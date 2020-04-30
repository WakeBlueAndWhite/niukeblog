package com.ceer.niukeblog.mapper;

import com.ceer.niukeblog.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByName(String name);

    User selectByEmail(String email);

    int updateStatus(@Param("status") Integer status, @Param("userId") Integer userId);
}