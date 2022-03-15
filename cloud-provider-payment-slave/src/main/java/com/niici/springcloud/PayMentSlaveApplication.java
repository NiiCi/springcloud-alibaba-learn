package com.niici.springcloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(value = "com.niici.springcloud.dao")
public class PayMentSlaveApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayMentSlaveApplication.class, args);
    }
}
