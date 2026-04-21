package com.jyw.csp.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
 
@Configuration
public class TopicRabbitConfig {
    //绑定键
    public final static String cspGatewayTopic = "topic.csp.monitor";
    
    public final static String topicExchange = "topicExchange";
 
    @Bean
    public Queue firstQueue() {
        return new Queue(TopicRabbitConfig.cspGatewayTopic);
    }
 
    @Bean
    TopicExchange exchange() {
        return new TopicExchange(TopicRabbitConfig.topicExchange);
    }
 
 
    //将firstQueue和topicExchange绑定,而且绑定的键值为topic.cspGateway
    //这样只要是消息携带的路由键是topic.cspGateway,才会分发到该队列
    @Bean
    Binding bindingExchangeMessage() {
        return BindingBuilder.bind(firstQueue()).to(exchange()).with(TopicRabbitConfig.cspGatewayTopic);
    }
 
    //将secondQueue和topicExchange绑定,而且绑定的键值为用上通配路由键规则topic.#
    // 这样只要是消息携带的路由键是以topic.开头,都会分发到该队列
//    @Bean
//    Binding bindingExchangeMessage2() {
//        return BindingBuilder.bind(firstQueue()).to(exchange()).with("topic.#");
//    }
 
}
