package com.tickatch.paymentservice.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_payment_detail")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PaymentDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

  @Column(nullable = false)
  private String paymentKey;

  public PaymentDetail(Payment payment) {
    this.payment = payment;
  }

  // paymentKey 업데이트
  public void updatePaymentKey(String paymentKey) {
    if (this.paymentKey != null) {
      throw new IllegalStateException("Payment key already set");
    }
    this.paymentKey = paymentKey;
  }
}
