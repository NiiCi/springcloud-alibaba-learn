package com.niici.springcloud.service;

import com.niici.springcloud.entity.Payment;

public interface PaymentService {
    public Payment queryById(Long id);

    public int createPayment(Payment payment);
}
