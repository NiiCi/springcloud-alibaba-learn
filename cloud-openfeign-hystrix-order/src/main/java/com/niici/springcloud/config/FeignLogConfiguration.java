package com.niici.springcloud.config;

import feign.Logger;

/**
 * feign日志配置类
 * 也可以在yml中配置
 * @author niici
 * @since 2022/03/30
 */
//@Configuration
public class FeignLogConfiguration {
    //@Bean
    Logger.Level feignLogLevel() {
        return Logger.Level.FULL;
    }
}
