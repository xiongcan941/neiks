package com.bbs.cloud.admin.test.controller;

import com.bbs.cloud.admin.common.contant.RabbitContant;
import com.bbs.cloud.admin.common.feigh.client.TestFeighClient;
import com.bbs.cloud.admin.common.result.HttpResult;
import com.bbs.cloud.admin.common.util.JedisUtil;
import com.bbs.cloud.admin.common.util.RedisOperator;
import com.bbs.cloud.admin.test.service.TestService;
import com.netflix.discovery.converters.Auto;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;

@RestController//返回值对应的是json数据,使用@RestController返回String,使用@RestController返回视图,
// 使用@Controller注解返回视图hello.html,@ResponseBody则返回信息（String)
// @Controller注解返回视图hello.html
//@ResponseBody能返回字符串和Map，@RestController相当于@Controller+@ResponseBody
//@RequestMapping可以处理很多中请求类型，具体如下，而@GetMapping只能接收Get请求，@PostMapping只能接收Post请求
@RequestMapping("test")
public class TestController {

    final static Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private TestService testService;

    @Autowired
    private JedisUtil jedisUtil;

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TestFeighClient testFeighClient;

    @GetMapping("/hello")
    public HttpResult hello(){
        logger.info("进入Helloe");
        return new HttpResult("hello world");
    }

    @GetMapping("/db")
    public HttpResult db(){
        logger.info("进入db层");
        return testService.queryTest();
    }

    @GetMapping("/redis")
    public HttpResult redis(){
        logger.info("进入redis接口");
        jedisUtil.set("key","value");
        logger.info("jedis输出{}",jedisUtil.get("key"));
        redisOperator.set("rediskey","redisvalue");
        logger.info("redisOperator输出{}",redisOperator.get("rediskey"));
        return HttpResult.ok();
    }

    @GetMapping("/mq")
    public HttpResult mq(){
        logger.info("进入mq层");
        rabbitTemplate.convertAndSend(RabbitContant.TEST_EXCHANGE_NAME,RabbitContant.TEST_ROUTING_KEY,"Hello world!");
        return testService.queryTest();
    }

    @GetMapping("/feigh")
    public HttpResult feign(){
        logger.info("进入feigh");
        return new HttpResult(testFeighClient.testFeigh());
    }
}
