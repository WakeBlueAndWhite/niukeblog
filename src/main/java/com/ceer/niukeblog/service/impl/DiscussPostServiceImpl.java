package com.ceer.niukeblog.service.impl;

import com.ceer.niukeblog.entity.DiscussPost;
import com.ceer.niukeblog.mapper.DiscussPostMapper;
import com.ceer.niukeblog.service.DiscussPostService;
import com.ceer.niukeblog.util.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *@ClassName DiscussPostServiceImpl
 *@Description TODO
 *@Author ceer
 *@Date 2020/4/29 14:34
 *@Version 1.0
 */
@Slf4j
@Service
public class DiscussPostServiceImpl implements DiscussPostService{

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 二级缓存: Redis -> mysql

                        log.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        log.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }
    @Override
    public int deleteByPrimaryKey(Integer id) {
        return discussPostMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(DiscussPost record) {
        if (record == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 转义HTML标记
        record.setTitle(HtmlUtils.htmlEscape(record.getTitle()));
        record.setContent(HtmlUtils.htmlEscape(record.getContent()));
        // 过滤敏感词
        record.setTitle(sensitiveFilter.filter(record.getTitle()));
        record.setContent(sensitiveFilter.filter(record.getContent()));
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
    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit,Integer orderMode) {
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }

        log.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit,orderMode);
    }


    /**
     * @Description: 查询文章总数
     * @param:
     * @return:
     * @date: 2020/4/29 16:09
     */
    @Override
    public int findDiscussPostRows(Integer userId) {
        if (userId == 0) {
            return postRowsCache.get(userId);
        }

        log.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    @Override
    public int updateCommentCount(Integer count, Integer id) {
        return discussPostMapper.updateCommentCount(count,id);
    }

    /**
     * @Description:
     * @param:
     * @return:
     * @date: 2020/5/8 22:58
     */
    @Override
    public int updateType(Integer id, Integer type) {
        return discussPostMapper.updateType(type,id);
    }

    @Override
    public int updateStatus(Integer id, Integer status) {
        return discussPostMapper.updateStatus(status,id);
    }

    @Override
    public int updateScore(Integer id, Double score) {
        return discussPostMapper.updateScore(score,id);
    }
}
