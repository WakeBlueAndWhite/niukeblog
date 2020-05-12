package com.ceer.niukeblog.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.ceer.niukeblog.config.RabbitMQConfig;
import com.ceer.niukeblog.entity.Message;
import com.ceer.niukeblog.service.MessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MQReceiver
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/6 16:55
 * @Version 1.0
 */
@Slf4j
@Component
@RabbitListener(queues = RabbitMQConfig.DirectQueueName)//监听的队列名称
public class MQReceiver {

    @Autowired
    private MessageService messageService;

    @RabbitHandler
    public void receive(String msg, Channel channel, org.springframework.amqp.core.Message message) throws IOException {
        // System.out.println("接收到的消息："+msg);
        try {
            if (msg == null) {
                log.error("消息的内容为空");
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }else {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        }catch(IOException e){
            //消费者处理出了问题，需要告诉队列信息消费失败
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
        Event event = JSONObject.parseObject(String.valueOf(msg), Event.class);
        if (event == null) {
            log.error("消息格式错误!");
            return;
        }
        // 发送站内通知
        Message ms = new Message();
        ms.setFromId(1);
        ms.setStatus(0);
        ms.setToId(event.getEntityUserId());
        ms.setConversationId(event.getTopic());
        ms.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            //entrySet是 键-值 对的集度合，Set里面的类型是Map.Entry
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        ms.setContent(JSONObject.toJSONString(content));
        messageService.insert(ms);
    }

}
