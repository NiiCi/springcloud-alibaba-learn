package com.niici.springcloud.controller;

import com.niici.springcloud.common.CommonResult;
import com.niici.springcloud.entity.Payment;
import com.niici.springcloud.service.PaymentOpenfeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
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
    public CommonResult<String> errorTest(@PathVariable("id") Long id) {
        paymentOpenfeignService.timeoutTest(id);
        return new CommonResult(200, "hystrix error test.");
    }
}
