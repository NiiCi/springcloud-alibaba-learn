package com.niici.springcloud.controller;

import com.niici.springcloud.common.CommonResult;
import com.niici.springcloud.entity.Payment;
import com.niici.springcloud.service.PaymentOpenfeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
}
