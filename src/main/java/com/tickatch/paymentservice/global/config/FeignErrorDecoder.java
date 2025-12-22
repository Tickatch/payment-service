package com.tickatch.paymentservice.global.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import io.github.tickatch.common.error.BusinessException;
import io.github.tickatch.common.error.GlobalErrorCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign Client 에러 디코더.
 *
 * <p>외부 서비스 호출 실패 시 HTTP 상태 코드에 따라 BusinessException으로 변환한다.
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder defaultDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    log.error(
        "Feign 호출 실패 - method: {}, status: {}, reason: {}",
        methodKey,
        response.status(),
        response.reason());

    return switch (response.status()) {
      case 400 -> new BusinessException(GlobalErrorCode.BAD_REQUEST, methodKey);
      case 401 -> new BusinessException(GlobalErrorCode.UNAUTHORIZED, methodKey);
      case 403 -> new BusinessException(GlobalErrorCode.FORBIDDEN, methodKey);
      case 404 -> new BusinessException(GlobalErrorCode.NOT_FOUND, methodKey);
      case 409 -> new BusinessException(GlobalErrorCode.CONFLICT, methodKey);
      case 422 -> new BusinessException(GlobalErrorCode.BUSINESS_ERROR, methodKey);
      case 503 -> new BusinessException(GlobalErrorCode.SERVICE_UNAVAILABLE, methodKey);
      case 504 -> new BusinessException(GlobalErrorCode.EXTERNAL_API_TIMEOUT, methodKey);
      default -> {
        if (response.status() >= 500) {
          yield new BusinessException(GlobalErrorCode.EXTERNAL_API_ERROR, methodKey);
        }
        yield defaultDecoder.decode(methodKey, response);
      }
    };
  }
}