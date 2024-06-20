package com.bbs.cloud.user.config;

import com.bbs.cloud.common.contant.RabbitContant;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitConfig {

    //创建交换机
    @Bean("bootExchange")
    public Exchange getExchange() {
        return ExchangeBuilder
                .topicExchange(RabbitContant.USER_EXCHANGE_NAME)//交换机类型 ;参数为名字
                .durable(true)//是否持久化，true即存到磁盘,false只在内存上
                .build();
    }

    //创建队列
    @Bean("bootQueue")
    public Queue getMessageQueue() {
        return new Queue(RabbitContant.USER_QUEUE_NAME);
    }

    //交换机绑定队列
    @Bean
    //@Qualifier注解,使用名称装配进行使用
    public Binding bindMessageQueue(@Qualifier("bootExchange") Exchange exchange, @Qualifier("bootQueue") Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(RabbitContant.USER_ROUTING_KEY)
                .noargs();
    }

}
