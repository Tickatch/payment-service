package com.tickatch.paymentservice.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "p_payment_detail")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
public abstract class PaymentDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column private String orderId;

  @Column private String paymentKey;
}
