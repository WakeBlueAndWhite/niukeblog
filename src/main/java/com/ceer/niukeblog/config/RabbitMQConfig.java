package com.ceer.niukeblog.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName RabbitMQConfig
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/6 16:17
 * @Version 1.0
 */
@Configuration
public class RabbitMQConfig {

    public static final String DirectQueueName = "directQueue";
    public static final String Direct_ES_QueueName = "direct_ES_Queue";
    public static final String Direct_DE_QueueName = "direct_DE_Queue";
    public static final String Direct_SH_QueueName = "direct_SH_Queue";
    public static final String DirectExchangeName = "directExchange";
    public static final String COMMENT_KEY = "comment";
    public static final String LIKE_KEY = "like";
    public static final String FOLLOW_KEY = "follow";
    public static final String PUBLISH_KEY = "publish";
    public static final String DELETE_KEY = "delete";
    public static final String SHARE_KEY = "share";


    /**
     * @Description: 队列
     * @param:
     * @return:
     * @date: 2020/5/6 16:41
     */
    @Bean
    public Queue directQueue() {
        // durable:是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启时仍然存在，暂存队列：当前连接有效
        // exclusive:默认也是false，只能被当前创建的连接使用，而且当连接关闭后队列即被删除。此参考优先级高于durable
        // autoDelete:是否自动删除，当没有生产者或者消费者使用此队列，该队列会自动删除。

        //一般设置一下队列的持久化就好,其余两个就是默认false
        return new Queue(DirectQueueName,true);
    }
    /**
     * @Description: 发送帖子将帖子存入es的queue
     * @param:
     * @return:
     * @date: 2020/5/7 23:32
     */
    @Bean
    public Queue directESQueue() {

        return new Queue(Direct_ES_QueueName,true);
    }
    @Bean
    public Queue directDeleteQueue() {

        return new Queue(Direct_DE_QueueName,true);
    }
    @Bean
    public Queue directShareQueue() {

        return new Queue(Direct_SH_QueueName,true);
    }
    /**
     * @Description: Direct交换机
     * @param:
     * @return:
     * @date: 2020/5/6 16:42
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DirectExchangeName,true,false);
    }
    /**
     * @Description: 绑定  将队列和交换机绑定,根据routingKey进行匹配
     * @param:
     * @return:
     * @date: 2020/5/6 16:42
     */
    @Bean
    public Binding bindingDirectComment() {
        return BindingBuilder.bind(directQueue()).to(directExchange()).with(COMMENT_KEY);
    }
    @Bean
    public Binding bindingDirectLike() {
        return BindingBuilder.bind(directQueue()).to(directExchange()).with(LIKE_KEY);
    }
    @Bean
    public Binding bindingDirectFollow() {
        return BindingBuilder.bind(directQueue()).to(directExchange()).with(FOLLOW_KEY);
    }
    @Bean
    public Binding bindingDirectPublish() {
        return BindingBuilder.bind(directESQueue()).to(directExchange()).with(PUBLISH_KEY);
    }
    @Bean
    public Binding bindingDirectDelete() {
        return BindingBuilder.bind(directDeleteQueue()).to(directExchange()).with(DELETE_KEY);
    }
    @Bean
    public Binding bindingDirectShare() {
        return BindingBuilder.bind(directShareQueue()).to(directExchange()).with(SHARE_KEY);
    }
}
