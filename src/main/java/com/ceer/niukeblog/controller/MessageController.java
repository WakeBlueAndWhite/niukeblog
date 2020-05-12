package com.ceer.niukeblog.controller;

import com.alibaba.fastjson.JSONObject;
import com.ceer.niukeblog.config.RabbitMQConfig;
import com.ceer.niukeblog.entity.Message;
import com.ceer.niukeblog.entity.Page;
import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.service.MessageService;
import com.ceer.niukeblog.service.UserService;
import com.ceer.niukeblog.util.CommunityUtil;
import com.ceer.niukeblog.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @ClassName MessageController
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/3 20:21
 * @Version 1.0
 */
@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     *
     * @param model
     * @param page
     * @return 私信列表
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.selectByPrimaryKey(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.selectByPrimaryKey(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.selectByPrimaryKey(id1);
        } else {
            return userService.selectByPrimaryKey(id0);
        }
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在!");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.insert(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();
        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), RabbitMQConfig.COMMENT_KEY);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            //将转义的JSON回转为JSON数据
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //将JSON转换为map
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.selectByPrimaryKey((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), RabbitMQConfig.COMMENT_KEY);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), RabbitMQConfig.COMMENT_KEY);
            messageVO.put("unread", unread);
            model.addAttribute("commentNotice", messageVO);
        }


        // 查询点赞类通知
        Message likeMessage = messageService.findLatestNotice(user.getId(), RabbitMQConfig.LIKE_KEY);
        if (likeMessage != null) {
            Map<String, Object> likeMessageVO = new HashMap<>();
            likeMessageVO.put("message", likeMessage);

            String content = HtmlUtils.htmlUnescape(likeMessage.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            likeMessageVO.put("user", userService.selectByPrimaryKey((Integer) data.get("userId")));
            likeMessageVO.put("entityType", data.get("entityType"));
            likeMessageVO.put("entityId", data.get("entityId"));
            likeMessageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), RabbitMQConfig.LIKE_KEY);
            likeMessageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), RabbitMQConfig.LIKE_KEY);
            likeMessageVO.put("unread", unread);
            model.addAttribute("likeNotice", likeMessageVO);
        }


        // 查询关注类通知
        Message followMessage = messageService.findLatestNotice(user.getId(), RabbitMQConfig.FOLLOW_KEY);
        if (followMessage != null) {
            Map<String, Object> followMessageVO = new HashMap<>();
            followMessageVO.put("message", followMessage);

            String content = HtmlUtils.htmlUnescape(followMessage.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            followMessageVO.put("user", userService.selectByPrimaryKey((Integer) data.get("userId")));
            followMessageVO.put("entityType", data.get("entityType"));
            followMessageVO.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), RabbitMQConfig.FOLLOW_KEY);
            followMessageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), RabbitMQConfig.FOLLOW_KEY);
            followMessageVO.put("unread", unread);
            model.addAttribute("followNotice", followMessageVO);
        }


        // 查询未读消息总数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    /**
     * @Description: 根据不同的topic查询数据分页显示
     * @param:
     * @return:
     * @date: 2020/5/6 22:58
     */
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();
        //设置分页数据
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));
        //查询当前用户的所选主题的通知列表
        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.selectByPrimaryKey((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
                map.put("fromUser", userService.selectByPrimaryKey(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}
