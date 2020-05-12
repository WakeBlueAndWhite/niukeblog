package com.ceer.niukeblog.service;

import com.ceer.niukeblog.entity.Message;

import java.util.List;

public interface MessageService{


    int deleteByPrimaryKey(Integer id);

    int insert(Message record);

    int insertSelective(Message record);

    Message selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Message record);

    int updateByPrimaryKey(Message record);

    List<Message> findConversations(int userId, int offset, int limit);

    int findConversationCount(int userId);

    List<Message> findLetters(String conversationId, int offset, int limit);

    int findLetterCount(String conversationId);

    int findLetterUnreadCount(int userId, String conversationId);

    int readMessage(List<Integer> ids);

    Message findLatestNotice(Integer userId,String topic);

    int findNoticeCount(Integer userId,String topic);

    int findNoticeUnreadCount(Integer userId,String topic);

    List<Message> findNotices(Integer userId, String topic, Integer offset, Integer limit);
}
