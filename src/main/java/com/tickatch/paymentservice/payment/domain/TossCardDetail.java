package com.tickatch.paymentservice.payment.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TOSS_CARD")
public class TossCardDetail extends PaymentDetail {

  private String billingKey;
}
