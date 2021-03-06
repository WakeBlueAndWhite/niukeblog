package com.ceer.niukeblog.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.ceer.niukeblog.config.RabbitMQConfig;
import com.ceer.niukeblog.entity.DiscussPost;
import com.ceer.niukeblog.service.DiscussPostService;
import com.ceer.niukeblog.service.ElasticSearchService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ClassName ESReceiver
 * @Description 监听发布帖子的队列信息然后将其存入es
 * @Author ceer
 * @Date 2020/5/7 23:35
 * @Version 1.0
 */
@Slf4j
@Component
@RabbitListener(queues = RabbitMQConfig.Direct_ES_QueueName)//监听的队列名称
public class ESReceiver {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticSearchService elasticSearchService;
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
        //获取帖子 将其存入ES
        DiscussPost post = discussPostService.selectByPrimaryKey(event.getEntityId());
        elasticSearchService.saveDiscussPost(post);
    }
}
