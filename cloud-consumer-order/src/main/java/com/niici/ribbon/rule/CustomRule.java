package com.niici.ribbon.rule;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 声明一个随机访问的负载均衡策略
 */
@Configuration
public class CustomRule {
    @Bean
    public IRule randomRule() {
        return new RandomRule();
    }
}
