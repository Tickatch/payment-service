package com.tickatch.paymentservice.payment.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentId {

  private UUID id;

  private PaymentId(UUID id) {
    this.id = id;
  }

  public static PaymentId of() {
    return of(null);
  }

  public static PaymentId of(UUID id) {
    id = Objects.requireNonNullElse(id, UUID.randomUUID());
    return new PaymentId(id);
  }

  public UUID toUuid() {
    return id;
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
