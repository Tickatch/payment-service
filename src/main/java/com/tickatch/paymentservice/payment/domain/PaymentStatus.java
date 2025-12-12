package com.tickatch.paymentservice.payment.domain;

public enum PaymentStatus {
  REQUESTED, // 결제 요청(아직 승인 전)
  SUCCESS, // 결제 성공
  CANCEL, // 결제 취소
  FAIL, // 결제 실패
  REFUND, // 환불 성공
  REFUND_FAIL, // 환불 실패
  EXPIRED, // 결제 시간 만료
  PROCESSING // 결제 처리 중
}
