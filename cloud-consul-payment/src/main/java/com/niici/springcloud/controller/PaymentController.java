package com.niici.springcloud.controller;

import com.niici.springcloud.common.CommonResult;
import com.niici.springcloud.entity.Payment;
import com.niici.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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

}
