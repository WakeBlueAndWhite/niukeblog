package com.ceer.niukeblog.util;

import com.ceer.niukeblog.entity.User;
import org.springframework.stereotype.Component;

/**
 * @ClassName HostHolder
 * @Description 持有用户信息,用于代替session对象.
 * @Author ceer
 * @Date 2020/5/1 1:20
 * @Version 1.0
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
