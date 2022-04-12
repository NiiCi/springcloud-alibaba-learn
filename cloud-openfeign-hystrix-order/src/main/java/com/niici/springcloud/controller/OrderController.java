package com.niici.springcloud.controller;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.niici.springcloud.common.CommonResult;
import com.niici.springcloud.entity.Payment;
import com.niici.springcloud.service.PaymentOpenfeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@DefaultProperties(defaultFallback = "globalFallbackHandle",
    commandProperties = {
        @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")
    })
public class OrderController {
    @Resource
    private PaymentOpenfeignService paymentOpenfeignService;

    /**
     * 调用eureka中注册的payment服务
     */
    @PostMapping("/addPayment")
    public CommonResult addPayment(@RequestBody Payment payment) {
        return paymentOpenfeignService.addPayment(payment);
    }

    @GetMapping("/query/{id}")
    public CommonResult query(@PathVariable("id") Long id) {
        return paymentOpenfeignService.queryPaymentById(id);
    }

    @PostMapping("/timeout")
    public CommonResult timeout() {
        // openfeign默认等待超时时间为1秒
        return paymentOpenfeignService.timeout();
    }

    /**
     * 定义一个用于测试hystrix 请求ok的方法
     * @return
     */
    @GetMapping("normalTest/{id}")
    public CommonResult<String> normalTest(@PathVariable("id") Long id) {
        paymentOpenfeignService.normalTest(id);
        return new CommonResult(200, "hystrix normal test.");
    }

    /**
     * 定义一个用于测试hystrix 超时降级的方法
     * @return
     */
    @GetMapping("timeoutTest/{id}")
    @HystrixCommand(fallbackMethod = "timeoutFallbackHandle",
            commandProperties = {
                    // 配置1.5s超时时间, 1.5s后未返回, 则走fallback方法
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1500")
            })
    public CommonResult<String> errorTest(@PathVariable("id") Long id) {
        paymentOpenfeignService.timeoutTest(id);
        return new CommonResult(200, "hystrix error test.");
    }

    /**
     * 定义一个用于测试hystrix 默认fallback降级方式的超时降级的方法
     * @return
     */
    @GetMapping("globalTest/{id}")
    @HystrixCommand
    public CommonResult<String> globalTest(@PathVariable("id") Long id) {
        paymentOpenfeignService.timeoutTest(id);
        return new CommonResult(200, "hystrix error test.");
    }

    // 定义fallback方法时, 方法的入参、返回值需要和原方法保持一致
    private CommonResult<String> timeoutFallbackHandle(@PathVariable("id") Long id) {
        log.info("timeoutFallbackHandle, 消费者端调用失败, 线程池: {}, payment id: {}", Thread.currentThread().getName(), id);
        return new CommonResult(200, "hystrix fallback method is execute.");
    }

    // 定义global fallback方法, 不需要指定入参
    private CommonResult<String> globalFallbackHandle() {
        log.info("globalFallbackHandle, 通用fallback处理, 消费者端调用失败.");
        return new CommonResult(200, "hystrix global fallback method is execute.");
    }

}
