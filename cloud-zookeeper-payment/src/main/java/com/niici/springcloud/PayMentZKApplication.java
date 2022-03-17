package com.niici.springcloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
// 使用@EnableDiscoveryClient注解，向注册中心注册服务
@EnableDiscoveryClient
@MapperScan(value = "com.niici.springcloud.dao")
public class PayMentZKApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayMentZKApplication.class, args);
    }
}
