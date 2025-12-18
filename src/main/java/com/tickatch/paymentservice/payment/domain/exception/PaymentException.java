package com.tickatch.paymentservice.payment.domain.exception;

import io.github.tickatch.common.error.BusinessException;
import io.github.tickatch.common.error.ErrorCode;

public class PaymentException extends BusinessException {

  public PaymentException(ErrorCode errorCode) {
    super(errorCode);
  }

  public PaymentException(ErrorCode errorCode, Object... errorArgs) {
    super(errorCode, errorArgs);
  }

  public PaymentException(ErrorCode errorCode, Throwable cause, Object... errorArgs) {
    super(errorCode, cause, errorArgs);
  }
}
