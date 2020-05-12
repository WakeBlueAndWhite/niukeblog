package com.ceer.niukeblog.mapper;

import com.ceer.niukeblog.entity.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MessageMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Message record);

    int insertSelective(Message record);

    Message selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Message record);

    int updateByPrimaryKey(Message record);

    /**
     *
     * @param userId
     * @param offset
     * @param limit
     * @return 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
     */
    List<Message> selectConversations(@Param("userId") Integer userId,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    /**
     *
     * @param userId
     * @return 查询当前用户的会话数量.
     */
    int selectConversationCount(int userId);

    /**
     *
     * @param conversationId
     * @param offset
     * @param limit
     * @return 查询某个会话所包含的私信列表.
     */
    List<Message> selectLetters(@Param("conversationId") String conversationId,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);

    /**
     *
     * @param conversationId
     * @return 查询某个会话所包含的私信数量.
     */
    int selectLetterCount(String conversationId);

    /**
     *
     * @param userId
     * @param conversationId
     * @return 查询未读私信的数量
     */
    int selectLetterUnreadCount(@Param("userId") Integer userId,
                                @Param("conversationId") String conversationId);


    /**
     *
     * @param ids
     * @param status
     * @return 修改消息的状态
     */
    int updateStatus(@Param("status") Integer status, @Param("ids") List<Integer> ids);

    /**
     *
     * @param userId
     * @param topic
     * @return 查询某个主题下最新的通知
     */
    Message selectLatestNotice(@Param("userId") Integer userId, @Param("topic") String topic);

    /**
     *
     * @param userId
     * @param topic
     * @return 查询某个主题所包含的通知数量
     */
    int selectNoticeCount(@Param("userId") Integer userId, @Param("topic") String topic);

    /**
     *
     * @param userId
     * @param topic
     * @return 查询未读的通知的数量
     */
    int selectNoticeUnreadCount(@Param("userId") Integer userId, @Param("topic") String topic);
    /**
     *
     * @param userId
     * @param topic
     * @return 查询某个主题所包含的通知列表
     */
    List<Message> selectNotices(@Param("userId") Integer userId, @Param("topic") String topic,@Param("offset") Integer offset, @Param("limit") Integer limit);
}