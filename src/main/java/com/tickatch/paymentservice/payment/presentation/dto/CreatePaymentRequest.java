package com.tickatch.paymentservice.payment.presentation.dto;

import com.tickatch.paymentservice.payment.application.dto.PaymentRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreatePaymentRequest(
    @NotBlank String orderName, @NotEmpty List<PaymentItem> payments) {

  public record PaymentItem(@NotBlank String reservationId, long price) {}

  public PaymentRequest toPaymentRequest() {
    List<PaymentRequest.PaymentItem> paymentItems =
        this.payments.stream()
            .map(p -> new PaymentRequest.PaymentItem(p.reservationId(), p.price()))
            .toList();

    return new PaymentRequest(orderName, paymentItems);
  }
}
