package com.bbs.cloud.admin.service.message;

import com.bbs.cloud.admin.common.contant.RabbitContant;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.controller.ServiceController;
import com.bbs.cloud.admin.service.message.dto.OrderMessageDTO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageReceiver {

    final static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    private List<MessageHandler> messageHandlers;

    @RabbitListener(queues = RabbitContant.SERVICE_QUEUE_NAME)
    public void receiver(String message){
        logger.info("接收到订单成功消息{}",message);
        System.out.println("message:"+message);
        if(StringUtils.isEmpty(message)){
            logger.info("接收到订单消息为空，消息:{}",message);
            return;
        }
        OrderMessageDTO orderMessageDTO;
        try {
            orderMessageDTO = JsonUtils.jsonToPojo(message, OrderMessageDTO.class);
            if(orderMessageDTO == null){
                logger.info("接收到订单消息转换为空，消息:{}",message);
                return;
            }
        }catch (Exception e){
            logger.info("接收到订单消息转换异常，消息:{}",message);
            e.printStackTrace();
            return;
        }
        Integer serviceType = orderMessageDTO.getServiceType();

        /**
         * TODO 消息处理未完成
         */
        //找到对应的实现类处理
        messageHandlers.stream()
                .filter(item -> item.getServiceType().equals(serviceType))
                .findFirst().get().handler(orderMessageDTO);
    }
}
