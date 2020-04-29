package com.ceer.niukeblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.ceer.niukeblog.mapper")
public class NiukeblogApplication {

    public static void main(String[] args) {
        SpringApplication.run(NiukeblogApplication.class, args);
    }

}
