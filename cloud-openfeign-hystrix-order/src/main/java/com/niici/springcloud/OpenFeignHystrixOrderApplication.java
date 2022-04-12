package com.niici.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
// 启用openfeign
@EnableFeignClients
// 客户端启用Hystrix
@EnableHystrix
public class OpenFeignHystrixOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenFeignHystrixOrderApplication.class, args);
    }
}
