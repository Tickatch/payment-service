package com.tickatch.paymentservice.payment.domain;

public enum LinkStatus {
  PENDING, // 결제 진행중 + 예매 확정은 아닌 상태
  CONFIRMED // 결제 완료 및 예매 확정 상태
}
