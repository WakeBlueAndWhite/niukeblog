package com.ceer.niukeblog.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @ClassName WkConfig
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/10 16:07
 * @Version 1.0
 */
@Slf4j
@Configuration
public class WkConfig {

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init() {
        // 创建WK图片目录
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdir();
            log.info("创建WK图片目录: " + wkImageStorage);
        }
    }
}
