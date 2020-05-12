package com.ceer.niukeblog.service.impl;

import com.ceer.niukeblog.entity.LoginTicket;
import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.enums.ActivationStatusEnum;
import com.ceer.niukeblog.mapper.LoginTicketMapper;
import com.ceer.niukeblog.mapper.UserMapper;
import com.ceer.niukeblog.service.UserService;
import com.ceer.niukeblog.util.CommunityUtil;
import com.ceer.niukeblog.util.HostHolder;
import com.ceer.niukeblog.util.MailClient;
import com.ceer.niukeblog.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName UserServiceImpl
 * @Description TODO
 * @Author ceer
 * @Date 2020/4/29 14:34
 * @Version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private HostHolder hostHolder;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return userMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(User record) {
        return userMapper.insert(record);
    }

    @Override
    public int insertSelective(User record) {
        return userMapper.insertSelective(record);
    }

    @Override
    public User selectByPrimaryKey(Integer id) {
        //return userMapper.selectByPrimaryKey(id);
        User user = getCache(id);
        if (user == null){
            user = initCache(id);
        }
        return user;
    }

    @Override
    public int updateByPrimaryKeySelective(User record) {
        return userMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(User record) {
        return userMapper.updateByPrimaryKey(record);
    }

    /**
     * @Description: 用户注册验证
     * @param:
     * @return:
     * @date: 2020/4/30 1:11
     */
    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册用户 随机生成盐 并将用户密码加密
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        //随机生成牛客网头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insert(user);

        // 激活邮件
        Context context = new Context();
        //将数据携带到页面回显
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        //将数据携带到页面回显
        context.setVariable("url", url);
        //使用thymeleaf模板引擎生成HTML页面
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    /**
     * @Description: 验证用户是否注册
     * @param:
     * @return:
     * @date: 2020/4/30 14:19
     */
    @Override
    public int activation(Integer userId, String code) {
        User user = userMapper.selectByPrimaryKey(userId);
        //1表示已注册
        if (user.getStatus() == 1) {
            return ActivationStatusEnum.ACTIVATION_REPEAT.getType();
        } else if (user.getActivationCode().equals(code)) {
            //用户的激活码与数据库一至 将用户状态更改未已注册
            userMapper.updateStatus(1, userId);
            clearCache(userId);
            return ActivationStatusEnum.ACTIVATION_SUCCESS.getType();
        } else {
            //注册失败
            return ActivationStatusEnum.ACTIVATION_FAILURE.getType();
        }
    }

    /**
     * @Description: 登录验证
     * @param:
     * @return:
     * @date: 2020/4/30 22:24
     */
    @Override
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
        }

        //校验账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "账号不存在！");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "账号未激活！");
            return map;
        }
        if (!user.getPassword().equals(CommunityUtil.md5(password + user.getSalt()))) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
        //loginTicketMapper.insert(loginTicket);
        //将登录凭证存入redis中
        String ticket = RedisKeyUtil.getTicket(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticket, loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * @Description: 退出登录将登录凭证设置为失效状态
     * @param:
     * @return:
     * @date: 2020/5/1 1:06
     */
    @Override
    public void logout(String ticket) {
//        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
//        if (loginTicket!=null){
//            loginTicketMapper.updateStatus(1,ticket);
//        }
        //获取登录凭证 退出时将status设置为 1
        String redisKey = RedisKeyUtil.getTicket(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    /**
     * @Description: 通过凭证获取凭证对象
     * @param:
     * @return:
     * @date: 2020/5/1 1:18
     */
    @Override
    public LoginTicket findLoginTicket(String ticket) {
//        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
//        return loginTicket;
        String redisKey = RedisKeyUtil.getTicket(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * @Description: 根据用户id更改用户头像
     * @param:
     * @return:
     * @date: 2020/5/1 15:53
     */
    @Override
    public void updateHeader(Integer id, String headerUrl) {
        int rows = userMapper.updateHeaderUrl(headerUrl, id);
        clearCache(id);
    }

    @Override
    public Map<String, Object> checkPassword(String oldPassword, User user) {
        Map<String, Object> map = new HashMap<>();
        String s = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!s.equals(user.getPassword())) {
            map.put("passwordMsg", "原始密码输入错误！请重新输入！！！");
            return map;
        }
        return null;
    }

    @Override
    public int updatePassword(String password, Integer id) {

        return userMapper.updatePassword(password, id);
    }

    @Override
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    /**
     * @Description: 从缓存中获取@param:
     * @return:
     * @date: 2020/5/5 19:39
     */
    private User getCache(Integer userId) {
        String redisKey = RedisKeyUtil.getUserkey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    
    /**
     * @Description: 取不到时初始化缓存数据
     * @param:
     * @return: 
     * @date: 2020/5/5 19:44
     */
    private User initCache(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        String redisKey = RedisKeyUtil.getUserkey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }
    /**
     * @Description: 数据变更时清除缓存数据
     * @param: 
     * @return: 
     * @date: 2020/5/5 19:44
     */
     private void clearCache(Integer userId) {
      String redisKey = RedisKeyUtil.getUserkey(userId);
        redisTemplate.delete(redisKey);
    }

    /**
     * @Description: 获取用户类型 将用户角色存入Security
     * @param:
     * @return:
     * @date: 2020/5/8 19:44
     */
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities(Integer userId) {
//        User user = this.selectByPrimaryKey(userId);
//
//        List<GrantedAuthority> list = new ArrayList<>();
//        list.add(new GrantedAuthority() {
//
//            @Override
//            public String getAuthority() {
//                switch (user.getType()) {
//                    case 1:
//                        return SecurityEnum.AUTHORITY_ADMIN.getRole();
//                    case 2:
//                        return SecurityEnum.AUTHORITY_MODERATOR.getRole();
//                    default:
//                        return SecurityEnum.AUTHORITY_USER.getRole();
//                }
//            }
//        });
//        return list;
//    }
}
