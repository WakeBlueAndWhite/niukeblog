package com.ceer.niukeblog.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.ceer.niukeblog.config.RabbitMQConfig;
import com.ceer.niukeblog.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @ClassName ShareReceiver
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/10 16:12
 * @Version 1.0
 */
@Slf4j
@Component
@RabbitListener(queues = RabbitMQConfig.Direct_SH_QueueName)
public class ShareReceiver {

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @RabbitHandler
    public void receive(String msg, Channel channel, org.springframework.amqp.core.Message message) throws IOException {
        try {
            if (msg == null) {
                log.error("消息的内容为空");
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }else {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (IOException e) {
            //消费者处理出了问题，需要告诉队列信息消费失败
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }

        Event event = JSONObject.parseObject(msg, Event.class);
        if (event == null) {
            log.error("消息格式错误!");
            return;
        }
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");
        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            log.info("生成长图成功: " + cmd);
        } catch (IOException e) {
            log.error("生成长图失败: " + e.getMessage());
        }
        // 启用定时器,监视该图片,一旦生成了,则上传至七牛云.
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

    class UploadTask implements Runnable {

        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值
        private Future future;
        // 开始时间
        private long startTime;
        // 上传次数
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            // 生成失败
            if (System.currentTimeMillis() - startTime > 30000) {
                log.error("执行时间过长,终止任务:" + fileName);
                future.cancel(true);
                return;
            }
            // 上传失败
            if (uploadTimes >= 3) {
                log.error("上传次数过多,终止任务:" + fileName);
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()) {
                log.info(String.format("开始第%d次上传[%s].", ++uploadTimes, fileName));
                // 设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone1()));
                try {
                    // 开始上传图片
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false);
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        log.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                    } else {
                        log.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch ( QiniuException e) {
                    log.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                }
            } else {
                log.info("等待图片生成[" + fileName + "].");
            }
        }
    }
}
