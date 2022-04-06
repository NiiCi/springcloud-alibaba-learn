package com.niici.springcloud.service;

import com.niici.springcloud.entity.Payment;

public interface PaymentService {
    Payment queryById(Long id);

    int createPayment(Payment payment);

    /**
     * hystrix 正常请求test
     * @return
     */
    void normalTest(Long paymentId);

    /**
     * hystrix 异常请求test
     * @return
     */
    void timeoutTest(Long paymentId);
}
