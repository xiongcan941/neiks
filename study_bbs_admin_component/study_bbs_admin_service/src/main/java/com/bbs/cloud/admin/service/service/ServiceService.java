package com.bbs.cloud.admin.service.service;


import com.bbs.cloud.admin.common.contant.RabbitContant;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.param.OrderMessageParam;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ServiceService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public HttpResult sendMessage(OrderMessageParam param){

        param.setDate(new Date());

        rabbitTemplate.convertAndSend(RabbitContant.SERVICE_EXCHANGE_NAME,RabbitContant.SERVICE_ROUTING_KEY, JsonUtils.objectToJson(param));

        return HttpResult.ok();
    }
}
