package com.niici.springcloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
// 启用熔断
@EnableCircuitBreaker
@MapperScan(value = "com.niici.springcloud.dao")
// @SpringCloudApplication 注解集成@EnableCircuitBreaker、@EnableEurekaClient、@SpringBootApplication
//@SpringCloudApplication
public class HystrixPayMentApplication {
    public static void main(String[] args) {
        SpringApplication.run(HystrixPayMentApplication.class, args);
    }
}
