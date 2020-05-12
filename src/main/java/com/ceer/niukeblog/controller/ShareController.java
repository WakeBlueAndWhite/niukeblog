package com.ceer.niukeblog.controller;

import com.ceer.niukeblog.config.RabbitMQConfig;
import com.ceer.niukeblog.rabbitmq.Event;
import com.ceer.niukeblog.rabbitmq.MQSender;
import com.ceer.niukeblog.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ShareController
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/10 16:09
 * @Version 1.0
 */
@Slf4j
@Controller
public class ShareController {

    @Autowired
    private MQSender mqSender;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    @RequestMapping(path = "/share", method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl) {
        // 文件名
        String fileName = CommunityUtil.generateUUID();

        // 异步生成长图
        Event event = new Event()
                .setTopic(RabbitMQConfig.SHARE_KEY)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");
         mqSender.sendMessage(event,event.getTopic(),RabbitMQConfig.DirectExchangeName);

        // 返回访问路径
        Map<String, Object> map = new HashMap<>();
        //map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        map.put("shareUrl", shareBucketUrl + "/" + fileName);

        return CommunityUtil.getJSONString(0, null, map);
    }

    // 废弃
    // 获取长图
    @RequestMapping(path = "/share/image/{fileName}", method = RequestMethod.GET)
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空!");
        }

        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("获取长图失败: " + e.getMessage());
        }
    }
}
