package com.niici.springcloud.service.impl;

import com.niici.springcloud.dao.PaymentDao;
import com.niici.springcloud.entity.Payment;
import com.niici.springcloud.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author niici
 */
@Service("paymentService")
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
}
