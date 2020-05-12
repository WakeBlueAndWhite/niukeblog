package com.ceer.niukeblog.service;

/**
 *@ClassName LikeService
 *@Description TODO
 *@Author ceer
 *@Date 2020/5/4 15:38
 *@Version 1.0
 */
public interface LikeService {

    void like(Integer userId, Integer entityType, Integer entityId, Integer entityUserId);

    long findEntityLikeCount(Integer entityType, Integer entityId);

    Integer findEntityLikeStatus(Integer userId, Integer entityType, Integer entityId);

    Integer findUserLikeCount(Integer userId);
}
