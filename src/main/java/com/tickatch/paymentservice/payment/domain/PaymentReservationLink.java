package com.tickatch.paymentservice.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_payment_reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentReservationLink {

  //  @EmbeddedId private PaymentReservationLinkId id;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 단일 PK

  @Column(name = "reservation_id", nullable = false)
  private String reservationId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_id")
  private Payment payment;

  @Column private Long amount;

  @Enumerated(EnumType.STRING)
  private LinkStatus status;

  public PaymentReservationLink(
      Payment payment, String reservationId, Long amount, LinkStatus status) {
    this.payment = payment;
    this.reservationId = reservationId;
    this.amount = amount;
    this.status = status;
  }

  public void confirm() {
    this.status = LinkStatus.CONFIRMED;
  }
}
