package com.bbs.cloud.admin.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer//代表注册中心，client为节点
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
