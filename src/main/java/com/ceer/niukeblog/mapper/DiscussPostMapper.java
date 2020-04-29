package com.ceer.niukeblog.mapper;

import com.ceer.niukeblog.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName DiscussPostMapper
 * @Description TODO
 * @Author ceer
 * @Date 2020/4/29 14:56
 * @Version 1.0
 */
@Mapper
public interface DiscussPostMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(DiscussPost record);

    int insertSelective(DiscussPost record);

    DiscussPost selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DiscussPost record);

    int updateByPrimaryKey(DiscussPost record);

    /**
     * @Description: 根据用户id查询用户文章并分页显示
     * @param:
     * @return:
     * @date: 2020/4/29 14:59
     */
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * @Description:
     * @param:
     * @return:
     * @date: 2020/4/29 15:00
     */
    int selectDiscussPostRows(@Param("userId") int userId);
}