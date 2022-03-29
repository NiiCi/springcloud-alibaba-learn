package com.niici.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
// 启用openfeign
@EnableFeignClients
public class OpenFeignOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenFeignOrderApplication.class, args);
    }
}
