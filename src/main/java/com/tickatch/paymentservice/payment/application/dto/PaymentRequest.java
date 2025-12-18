package com.tickatch.paymentservice.payment.application.dto;

import java.util.List;

public record PaymentRequest(String orderName, List<PaymentItem> payments) {

  public record PaymentItem(String reservationId, long price) {}
}
