package com.niici.springcloud.service.impl;

import cn.hutool.core.util.IdUtil;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.niici.springcloud.dao.PaymentDao;
import com.niici.springcloud.entity.Payment;
import com.niici.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author niici
 */
@Service("paymentService")
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private PaymentDao paymentDao;

    @Override
    public Payment queryById(Long id) {
        return paymentDao.queryById(id);
    }

    @Override
    public int createPayment(Payment payment) {
        return paymentDao.createPayment(payment);
    }

    @Override
    public void normalTest(Long paymentId) {
        log.info("normalTest, 线程池: {}, payment id: {}", Thread.currentThread().getName(), paymentId);
    }

    @Override
    // 方法上添加@HystrixCommond注解, 指定fallback方法, 已经对应的属性
    // fallback方法指定降级时调用的方法
    @HystrixCommand(fallbackMethod = "timeoutFallbackHandle", commandProperties = {
            // HystrixProperty属性在HystrixPropertiesManager中可以找到
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")
    })
    public void timeoutTest(Long paymentId) {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.error("timeoutTest, 线程池: {}, payment id: {}", Thread.currentThread().getName(), paymentId);
    }

    public void timeoutFallbackHandle(Long paymentId) {
        log.info("timeoutFallbackHandle, 线程池: {}, payment id: {}", Thread.currentThread().getName(), paymentId);
    }

    @HystrixCommand(fallbackMethod = "paymentCircuitBreakerFallback", commandProperties = {
            // HystrixProperty属性在HystrixPropertiesManager中可以找到, HystrixCommondProperties中可以查看每个属性的默认值
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"), // 是否开启断路器
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"), // 滑动窗口中, 最少有多少个请求, 才可能触发熔断
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"), // 熔断后多少时间内直接reject请求, 之后进入half-open状态, 默认为5s
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60") // 异常比例达到多少, 触发熔断, 默认比例为50%
    })
    @Override
    public String paymentCircuitBreaker(Long paymentId) {
        if (paymentId < 0) {
            throw new RuntimeException("payment服务熔断测试, id不能为负数");
        }
        String simpleUUID = IdUtil.simpleUUID();
        return Thread.currentThread().getName() + ", 调用成功, 流水号: " + simpleUUID;
    }

    /**
     * 熔断降级方法
     * @param paymentId
     * @return
     */
    public String paymentCircuitBreakerFallback(Long paymentId) {
        return "payment服务熔断测试, id不能为负数, id: " + paymentId;
    }
}
