package com.tickatch.paymentservice.payment.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("TOSS_CARD")
@NoArgsConstructor
public class TossCardDetail extends PaymentDetail {

  public TossCardDetail(Payment payment, String paymentKey) {
    super(payment);
    updatePaymentKey(paymentKey);
    payment.assignDetail(this);
  }

  public static TossCardDetail create(Payment payment, String paymentKey) {
    return new TossCardDetail(payment, paymentKey);
  }
}
