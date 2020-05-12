package com.ceer.niukeblog.interceptor;

import com.ceer.niukeblog.entity.LoginTicket;
import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.service.UserService;
import com.ceer.niukeblog.util.CookieUtil;
import com.ceer.niukeblog.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @ClassName LoginTicketInterceptor
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/1 1:13
 * @Version 1.0
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;
    /**
     * @Description: 在Controller之前执行
     * @param:
     * @return:
     * @date: 2020/5/1 1:22
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket != null) {
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 根据凭证查询用户
                User user = userService.selectByPrimaryKey(loginTicket.getUserId());
                // 在本次请求中持有用户
                hostHolder.setUser(user);
//                // 构建用户认证的结果,并存入SecurityContext,以便于Security进行授权.
//                Authentication authentication = new UsernamePasswordAuthenticationToken(
//                        user, user.getPassword(), userService.getAuthorities(user.getId()));
//                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    /**
     * @Description: 在Controller之后执行
     * @param:
     * @return:
     * @date: 2020/5/1 1:22
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    /**
     * @Description: 在TemplateEngine之后执行
     * @param:
     * @return:
     * @date: 2020/5/1 1:22
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
//        SecurityContextHolder.clearContext();
    }


}
