package com.niici.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import com.niici.ribbon.rule.CustomRule;

@SpringBootApplication
//@RibbonClients(value = {@RibbonClient(value = "random", configuration = CustomRule.class)})
// 添加注解@RibbonClient, 使用自定义的负载均衡策略
// value表示被调用的服务, 与eureka中注册的服务名相同(需要大写)
// configuration 表示所使用的配置类
@RibbonClient(value = "PAYMENT-SERVICE", configuration = CustomRule.class)
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
