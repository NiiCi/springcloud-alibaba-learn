package com.niici.springcloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan(value = "com.niici.springcloud.dao")
public class PaymentConsulApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentConsulApplication.class, args);
    }
}
