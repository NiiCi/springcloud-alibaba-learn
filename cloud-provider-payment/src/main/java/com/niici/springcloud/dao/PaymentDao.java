package com.niici.springcloud.dao;

import com.niici.springcloud.entity.Payment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author niici
 */
@Repository
public interface PaymentDao {
    public Payment queryById(@Param("id") Long id);

    public int createPayment(Payment payment);
}
