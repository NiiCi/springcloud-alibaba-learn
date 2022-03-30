package com.niici.springcloud.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * feign日志配置类
 * @author niici
 * @since 2022/03/30
 */
@Configuration
public class FeignLogConfiguration {
    @Bean
    Logger.Level feignLogLevel() {
        return Logger.Level.FULL;
    }
}
