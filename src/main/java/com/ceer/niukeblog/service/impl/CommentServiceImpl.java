package com.ceer.niukeblog.service.impl;

import com.ceer.niukeblog.enums.CommentEnum;
import com.ceer.niukeblog.service.DiscussPostService;
import com.ceer.niukeblog.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.ceer.niukeblog.mapper.CommentMapper;
import com.ceer.niukeblog.entity.Comment;
import com.ceer.niukeblog.service.CommentService;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService{

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return commentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insert(comment);

        // 更新帖子评论数量
        if (comment.getEntityType() == CommentEnum.ENTITY_TYPE_POST.getType()) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    @Override
    public int insertSelective(Comment record) {
        return commentMapper.insertSelective(record);
    }

    @Override
    public Comment selectByPrimaryKey(Integer id) {
        return commentMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Comment record) {
        return commentMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Comment record) {
        return commentMapper.updateByPrimaryKey(record);
    }

    /**
     * @Description: 查询回复并分页显示
     * @param:
     * @return:
     * @date: 2020/5/2 22:53
     */
    @Override
    public List<Comment> findCommentsByEntity(Integer entityType, Integer entityId, Integer offset, Integer limit) {
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    /**
     * @Description: 查询回复中评论的数量
     * @param:
     * @return:
     * @date: 2020/5/2 22:52
     */
    @Override
    public int findCommentCount(Integer entityType, Integer entityId) {
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    /**
     * @Description: 查询文章的回复数量 entityType为1的数据
     * @param:
     * @return:
     * @date: 2020/5/2 22:52
     */
//    @Override
//    public int findReplyCount(Integer entityType) {
//        return commentMapper.selectCountByReply(entityType);
//    }


}
