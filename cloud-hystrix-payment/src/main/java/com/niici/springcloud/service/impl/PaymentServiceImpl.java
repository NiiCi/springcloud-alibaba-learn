package com.niici.springcloud.service.impl;

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
        //int age = 10 / 0;
        log.error("timeoutTest, 线程池: {}, payment id: {}", Thread.currentThread().getName(), paymentId);
    }

    public void timeoutFallbackHandle(Long paymentId) {
        log.info("timeoutFallbackHandle, 线程池: {}, payment id: {}", Thread.currentThread().getName(), paymentId);
    }
}
