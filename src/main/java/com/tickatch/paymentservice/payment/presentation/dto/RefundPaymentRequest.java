package com.tickatch.paymentservice.payment.presentation.dto;

import com.tickatch.paymentservice.payment.application.dto.RefundRequest;
import com.tickatch.paymentservice.payment.domain.RefundReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RefundPaymentRequest(@NotBlank String reason, @NotEmpty List<String> reservationIds) {

  public RefundReason toRefundReason() {
    return RefundReason.valueOf(reason.toUpperCase());
  }

  public RefundRequest toRefundRequest() {
    return new RefundRequest(toRefundReason(), reservationIds());
  }
}
