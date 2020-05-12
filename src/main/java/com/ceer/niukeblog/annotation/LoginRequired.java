package com.ceer.niukeblog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *@ClassName LoginRequired
 *@Description 拦截未登录的不合理访问
 *@Author ceer
 *@Date 2020/5/1 17:28
 *@Version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}
