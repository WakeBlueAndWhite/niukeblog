package com.ceer.niukeblog.service.impl;

import com.ceer.niukeblog.entity.Message;
import com.ceer.niukeblog.mapper.MessageMapper;
import com.ceer.niukeblog.service.MessageService;
import com.ceer.niukeblog.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService{

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return messageMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insert(message);
    }

    @Override
    public int insertSelective(Message record) {
        return messageMapper.insertSelective(record);
    }

    @Override
    public Message selectByPrimaryKey(Integer id) {
        return messageMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Message record) {
        return messageMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Message record) {
        return messageMapper.updateByPrimaryKey(record);
    }


    @Override
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    @Override
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    @Override
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    @Override
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    @Override
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    @Override
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(1,ids);
    }

    /**
     *
     * @param userId
     * @param topic
     * @return 查询某个主题下最新的通知
     */
    @Override
    public Message findLatestNotice(Integer userId, String topic){
        return messageMapper.selectLatestNotice(userId,topic);
    }

    /**
     *
     * @param userId
     * @param topic
     * @return 查询某个主题所包含的通知数量
     */
    @Override
    public int findNoticeCount(Integer userId, String topic){
        return messageMapper.selectNoticeCount(userId,topic);
    }

    /**
     *
     * @param userId
     * @param topic
     * @return 查询未读的通知的数量
     */
    @Override
    public int findNoticeUnreadCount(Integer userId, String topic){
        return messageMapper.selectNoticeUnreadCount(userId,topic);
    }
    /**
     *
     * @param userId
     * @param topic
     * @return 查询某个主题所包含的通知列表
     */
    @Override
    public List<Message> findNotices(Integer userId, String topic, Integer offset, Integer limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
