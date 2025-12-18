package com.tickatch.paymentservice.payment.domain.exception;

import io.github.tickatch.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
  INVALID_STATUS_FOR_SUCCESS(HttpStatus.BAD_REQUEST.value(), "INVALID_STATUS_FOR_SUCCESS"),
  INVALID_STATUS_FOR_CANCEL(HttpStatus.BAD_REQUEST.value(), "INVALID_STATUS_FOR_CANCEL"),
  INVALID_STATUS_FOR_FAIL(HttpStatus.BAD_REQUEST.value(), "INVALID_STATUS_FOR_FAIL"),
  INVALID_STATUS_FOR_REFUND(HttpStatus.BAD_REQUEST.value(), "INVALID_STATUS_FOR_REFUND"),
  INVALID_STATUS_FOR_REFUND_FAIL(HttpStatus.BAD_REQUEST.value(), "INVALID_STATUS_FOR_REFUND_FAIL"),
  INVALID_STATUS_FOR_EXPIRED(HttpStatus.BAD_REQUEST.value(), "INVALID_STATUS_FOR_EXPIRED"),
  INVALID_STATUS_FOR_PROCESSING(HttpStatus.BAD_REQUEST.value(), "INVALID_STATUS_FOR_PROCESSING"),

  NO_RESERVATION_LINK(HttpStatus.BAD_REQUEST.value(), "NO_RESERVATION_LINK"),
  INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST.value(), "INVALID_PAYMENT_AMOUNT"),
  INVALID_PAYMENT_METHOD(HttpStatus.BAD_REQUEST.value(), "INVALID_PAYMENT_METHOD"),
  DUPLICATE_RESERVATION_ID(HttpStatus.BAD_REQUEST.value(), "DUPLICATE_RESERVATION_ID"),
  ;

  private final int status;
  private final String code;
}
