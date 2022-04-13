package com.niici.springcloud.controller;

import com.niici.springcloud.common.CommonResult;
import com.niici.springcloud.entity.Payment;
import com.niici.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("query/{id}")
    public CommonResult<Payment> queryPaymentById(@PathVariable("id") Long id) {
        Payment payment = paymentService.queryById(id);
        return new CommonResult<>(200, "SUCCESS, server port:" + serverPort, payment);
    }

    @PostMapping("addPayment")
    public CommonResult addPayment(@RequestBody Payment payment) {
        paymentService.createPayment(payment);
        return new CommonResult<>(200, "SUCCESS, server port:" + serverPort);
    }

    /**
     * 定义一个用于测试openfeign超时的接口
     * @return
     */
    @PostMapping("timeout")
    public CommonResult<String> timeout() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            log.error("test openfeign timeout");
        }
        return new CommonResult(200, "request is timeout, server port:" + serverPort);
    }

    /**
     * 定义一个用于测试hystrix 请求ok的方法
     * @return
     */
    @GetMapping("normalTest/{id}")
    public CommonResult<String> normalTest(@PathVariable("id") Long id) {
        paymentService.normalTest(id);
        return new CommonResult(200, "hystrix normal test.");
    }

    /**
     * 定义一个用于测试hystrix 超时降级的方法
     * @return
     */
    @GetMapping("timeoutTest/{id}")
    public CommonResult<String> errorTest(@PathVariable("id") Long id) {
        paymentService.timeoutTest(id);
        return new CommonResult(200, "hystrix error test.");
    }

    /**
     * 定义一个用于测试hystrix 服务熔断的方法
     * 如何测试：
     * 1. 用负数请求10次以后, 触发服务熔断, 10s内的所有请求都会被拒绝, 此时用正数请求直接调用fallback方法
     * 2. 10s之后用正数请求, 正常返回, 服务恢复
     * @return
     */
    @GetMapping("circuitBreaker/{id}")
    public CommonResult<String> paymentCircuitBreaker(@PathVariable("id") Long id) {
        return new CommonResult(200, paymentService.paymentCircuitBreaker(id));
    }

}
