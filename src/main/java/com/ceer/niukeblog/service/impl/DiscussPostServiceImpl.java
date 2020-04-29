package com.ceer.niukeblog.service.impl;

import com.ceer.niukeblog.entity.DiscussPost;
import com.ceer.niukeblog.mapper.DiscussPostMapper;
import com.ceer.niukeblog.service.DiscussPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *@ClassName DiscussPostServiceImpl
 *@Description TODO
 *@Author ceer
 *@Date 2020/4/29 14:34
 *@Version 1.0
 */
@Service
public class DiscussPostServiceImpl implements DiscussPostService{

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return discussPostMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(DiscussPost record) {
        return discussPostMapper.insert(record);
    }

    @Override
    public int insertSelective(DiscussPost record) {
        return discussPostMapper.insertSelective(record);
    }

    @Override
    public DiscussPost selectByPrimaryKey(Integer id) {
        return discussPostMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(DiscussPost record) {
        return discussPostMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(DiscussPost record) {
        return discussPostMapper.updateByPrimaryKey(record);
    }
    
    /**
     * @Description: 根据用户id查询用户文章并分页显示
     * @param: 
     * @return: 
     * @date: 2020/4/29 16:07
     */
    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    /**
     * @Description: 查询文章总数
     * @param:
     * @return:
     * @date: 2020/4/29 16:09
     */
    @Override
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }


}
