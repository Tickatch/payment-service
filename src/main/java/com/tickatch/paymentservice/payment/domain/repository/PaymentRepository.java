package com.tickatch.paymentservice.payment.domain.repository;

import com.tickatch.paymentservice.payment.domain.Payment;
import com.tickatch.paymentservice.payment.domain.PaymentId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, PaymentId> {

  Optional<Payment> findByOrderId(UUID orderId);
}
