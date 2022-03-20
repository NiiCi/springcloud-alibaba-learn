package com.niici.springcloud.controller;

import com.niici.springcloud.common.CommonResult;
import com.niici.springcloud.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RestController
@Slf4j
public class OrderController {
    @Resource
    private RestTemplate restTemplate;

    private static final String ADD_PAY_MENT_URL = "/payment/addPayment";

    private static final String QUERY_PAY_MENT_URL = "/payment/query/";

    //private static final String HOST = "http://localhost:8001";

    /**
     * 调用eureka中注册的payment服务
     * 需要配合@LoadBalanced注解使用，加在restTemplate上，实现负载均衡，否则会报unknowhostException
     */
    private static final String HOST = "http://zk-payment-service";

    @PostMapping("/addPayment")
    public CommonResult addPayment(@RequestBody Payment payment) {
        return restTemplate.postForObject(HOST + ADD_PAY_MENT_URL, payment, CommonResult.class);
    }

    @GetMapping("/query/{id}")
    public CommonResult query(@PathVariable("id") Long id) {
        return restTemplate.getForObject(HOST + QUERY_PAY_MENT_URL + id, CommonResult.class);
    }
}
