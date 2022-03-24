package com.niici.springcloud.service;

import com.niici.springcloud.entity.Payment;
import org.apache.ibatis.annotations.Param;

public interface PaymentService {
    public Payment queryById(Long id);

    public int createPayment(Payment payment);
}
