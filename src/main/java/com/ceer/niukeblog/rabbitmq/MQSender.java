package com.ceer.niukeblog.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.ceer.niukeblog.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @ClassName MQsender
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/6 16:52
 * @Version 1.0
 */
@Slf4j
@Component
public class MQSender implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @Description: 将事件发送到指定的路由键
     * @param:
     * @return:
     * @date: 2020/5/6 17:06
     */
    public void sendMessage(Event event,String routingKey,String exchangeName){
        //消息发送失败返回到队列中, yml需要配置 publisher-returns: true
        rabbitTemplate.setMandatory(true);
        //消息消费者确认收到消息后，手动ack回执
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, JSONObject.toJSONString(event));
    }
    /**
     * 如果消息没有到达交换机,则该方法中ack = false,error为错误信息;
     * 如果消息正确到达交换机,则该方法中ack = true;
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        //log.info("confirm 回调方法，回调消息id：" + correlationData.getId());
        if (ack) {
            log.info("confirm 消息发送到交换机成功。。");
        } else {
            log.info("confirm 消息发送到交换机失败，原因是：[{}]", cause);
        }
    }
    /**
     * 消息从交换机成功到达队列，则returnedMessage方法不会执行;
     * 消息从交换机未能成功到达队列，则returnedMessage方法会执行;
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText,  String exchange, String routingKey) {
        log.info("returnedMessage 消息没有达到队列: " + new String(message.getBody(), StandardCharsets.UTF_8) + " ," +
                " replyCode= " + replyCode + " , " + " replyText= " + replyText, " exchange= " + exchange + " routingKey= " + routingKey);
    }
}
