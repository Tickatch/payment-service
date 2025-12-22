package com.tickatch.paymentservice.payment.application;

import com.tickatch.paymentservice.payment.domain.PaymentMethod;
import java.util.UUID;

public interface PaymentLogEventPublisher {
  void publish(UUID paymentId, PaymentMethod method, int retryCount, String actionType);
}
