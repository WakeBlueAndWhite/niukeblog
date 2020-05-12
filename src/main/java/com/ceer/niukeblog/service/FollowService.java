package com.ceer.niukeblog.service;


import java.util.List;
import java.util.Map;

/**
 *@ClassName FollowService
 *@Description TODO
 *@Author ceer
 *@Date 2020/5/4 21:30
 *@Version 1.0
 */
public interface FollowService {

    void follow(Integer userId, Integer entityType, Integer entityId);

    void unfollow(Integer userId, Integer entityType, Integer entityId);

    long findFolloweeCount(Integer userId, Integer entityType);

    long findFollowerCount(Integer entityType, Integer entityId);

    boolean hasFollowed(Integer userId, Integer entityType, Integer entityId);

    List<Map<String,Object>> findFollowees(Integer userId, Integer offset, Integer limit);

    List<Map<String,Object>> findFollowers(Integer userId, Integer offset, Integer limit);
}
