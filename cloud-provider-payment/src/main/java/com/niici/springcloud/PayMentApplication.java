package com.niici.springcloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "com.niici.springcloud.dao")
public class PayMentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayMentApplication.class, args);
    }
}
