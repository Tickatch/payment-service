package com.tickatch.paymentservice.payment.domain;

public enum RefundReason {
  CUSTOMER_CANCEL, // 사용자 예매 취소로 인한 환불
  PRODUCT_CANCEL // 상품 취소로 인한 환불
}
