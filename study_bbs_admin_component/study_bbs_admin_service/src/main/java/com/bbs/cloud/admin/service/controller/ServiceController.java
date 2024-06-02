package com.bbs.cloud.admin.service.controller;

import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.JsonUtils;
import com.bbs.cloud.admin.service.param.OrderMessageParam;
import com.bbs.cloud.admin.service.service.ServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("service")
public class ServiceController {

    final static Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private ServiceService serviceService;

    @PostMapping("/send/message")
    public HttpResult sendMessage(@RequestBody OrderMessageParam param){
        logger.info("进入接收订单消息接口，请求参数{}", JsonUtils.objectToJson(param));
        return serviceService.sendMessage(param);
    }

    @GetMapping("/query")
    public HttpResult serviceQuery(){
        return null;
    }

}
