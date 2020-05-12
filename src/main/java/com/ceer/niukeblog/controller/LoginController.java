package com.ceer.niukeblog.controller;

import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.enums.ActivationStatusEnum;
import com.ceer.niukeblog.enums.LoginExpiredEnum;
import com.ceer.niukeblog.service.UserService;
import com.ceer.niukeblog.util.CommunityUtil;
import com.ceer.niukeblog.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName LoginController
 * @Description TODO
 * @Author ceer
 * @Date 2020/4/30 1:07
 * @Version 1.0
 */
@Slf4j
@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * @Description: 注册页面
     * @param:
     * @return:
     * @date: 2020/4/30 14:43
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * @Description: 登录页面
     * @param:
     * @return:
     * @date: 2020/4/30 14:44
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * @Description: 注册账号
     * @param:
     * @return:
     * @date: 2020/4/30 14:42
     */
    @PostMapping("/register")
    public String register(Model model, User user) {
        //获取用户注册的反馈信息 如果为空表示注册成功 否则表示不符合注册规范
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }
    /**
     * @Description: 账户激活验证
     * @param:
     * @return:
     * @date: 2020/4/30 14:42
     */
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") Integer userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ActivationStatusEnum.ACTIVATION_SUCCESS.getType()){
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        }else if (result == ActivationStatusEnum.ACTIVATION_REPEAT.getType()){
            model.addAttribute("msg","无效操作，该账号已经激活过了");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * @Description: 生成随机验证码
     * @param:
     * @return:
     * @date: 2020/4/30 17:31
     */
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        // 将验证码存入session
        //session.setAttribute("kaptcha", text);
        //验证码归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入redis中
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);
        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            log.error("响应验证码失败:" + e.getMessage());
        }
    }

    /**
     * @Description: 登录验证 验证账号，密码 ，图形码  并设置账号的登录失效时间
     * @param:
     * @return:
     * @date: 2020/4/30 22:35
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model,HttpServletResponse response,@CookieValue("kaptchaOwner")String kaptchaOwner) {
        // 检查验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNoneBlank(kaptchaOwner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        //设置失效时间 如果记住我被选中 失效时间为 1个月 否则为 1天
        int expiredSeconds = rememberme ? LoginExpiredEnum.REMEMBER_EXPIRED_SECONDS.getTime()
                                        : LoginExpiredEnum.DEFAULT_EXPIRED_SECONDS.getTime();
        // 检查账号,密码
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * @Description: 登出将用户凭证设置为失效
     * @param:
     * @return:
     * @date: 2020/5/1 1:10
     */
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket")String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }
}
