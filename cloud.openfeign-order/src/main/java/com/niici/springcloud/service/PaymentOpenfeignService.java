package com.niici.springcloud.service;

import com.niici.springcloud.common.CommonResult;
import com.niici.springcloud.entity.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// 指定调用的服务
@FeignClient(value = "PAYMENT-SERVICE")
public interface PaymentOpenfeignService {

    // 声明payment服务controller中的接口
    // 此处的路径即为所要调用的服务的全路径 /payment 不能缺
    @GetMapping("/payment/query/{id}")
    CommonResult<Payment> queryPaymentById(@PathVariable("id") Long id);

    // 声明payment服务controller中的接口
    @PostMapping("/payment/addPayment")
    CommonResult addPayment(@RequestBody Payment payment);

    // 用于测试openfeign超时的接口
    @PostMapping("/payment/timeout")
    CommonResult timeout();
}
