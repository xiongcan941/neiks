package com.bbs.cloud.user.message;

import com.bbs.cloud.common.contant.RabbitContant;
import com.bbs.cloud.common.contant.RedisContant;
import com.bbs.cloud.common.message.user.UserMessageDTO;
import com.bbs.cloud.common.util.JedisUtil;
import com.bbs.cloud.common.util.JsonUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MessageReceiver implements ApplicationRunner {

    final static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    @Autowired
    private List<MessageHandler> messageHandlers;
    private ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("user--%d")
            .build();

    //线程池去处理

    ThreadPoolExecutor executorPool = new ThreadPoolExecutor(10,
            50,
            10,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(50),
            threadFactory
    );

    @Autowired
    private JedisUtil jedisUtil;

    private static final AtomicInteger atomicInteger = new AtomicInteger(0);

    @RabbitListener(queues = RabbitContant.USER_QUEUE_NAME)
    public void receiver(String message) {
        logger.info("接受用户产生的消息:{}", message);
        if (StringUtils.isEmpty(message)) {
            logger.info("接受用户产生的消息, 消息为空:{}", message);
            return;
        }
        UserMessageDTO userMessageDTO;
        try {
            userMessageDTO = JsonUtils.jsonToPojo(message, UserMessageDTO.class);
            if (userMessageDTO == null) {
                logger.info("接受用户产生的消息, 消息转换为空:{}", message);
                return;
            }
        } catch (Exception e) {
            logger.info("接受用户产生的消息, 消息转换异常:{}", message);
            e.printStackTrace();
            return;
        }

        jedisUtil.lpush(RedisContant.BBS_CLOUD_USER_MESSAGE_LIST, message);

    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        Thread thread = new Thread(() -> {
            dealRedisMessage();
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 大家要做一个守护线程
     * 1、统计线程池执行任务的线程数量
     * 2、如果线程数量小于50，我们就需要从redis列表中获取数据
     */
    public void dealRedisMessage() {
        while (true) {
            if (jedisUtil.llen(RedisContant.BBS_CLOUD_USER_MESSAGE_LIST) < 1) {
                continue;
            }
            if (atomicInteger.get() < 49) {
                String message = jedisUtil.lpop(RedisContant.BBS_CLOUD_USER_MESSAGE_LIST);

                UserMessageDTO userMessageDTO = null;

                if (StringUtils.isEmpty(message)) {
                    logger.info("从redis中获取消息-user消息为空, message:{}", message);
                    continue;
                }
                try {
                    userMessageDTO = JsonUtils.jsonToPojo(message, UserMessageDTO.class);
                    if (userMessageDTO == null) {
                        logger.info("接收user-从redis中获取消息，转换为空, message:{}", message);
                        continue;
                    }
                } catch (Exception e) {
                    logger.info("接收user-从redis中获取消息，转换异常, message:{}", message);
                    e.printStackTrace();
                }

                String type = userMessageDTO.getType();

                UserMessageDTO finalUserMessageDTO = userMessageDTO;

                executorPool.execute(() -> {
                    atomicInteger.incrementAndGet();
                    MessageHandler messageManage = messageHandlers.stream()
                            .filter(item -> item.getMessageType().equals(type))
                            .findFirst().get();
                    messageManage.handler(finalUserMessageDTO);
                    atomicInteger.decrementAndGet();
                });
            }
        }

    }


}
