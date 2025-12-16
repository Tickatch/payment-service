package com.tickatch.paymentservice.payment.presentation.api;

import com.tickatch.paymentservice.payment.application.service.PaymentService;
import com.tickatch.paymentservice.payment.presentation.dto.CreatePaymentRequest;
import com.tickatch.paymentservice.payment.presentation.dto.RefundPaymentRequest;
import io.github.tickatch.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentApi {

  private final PaymentService paymentService;

  // 1. 결제 생성
  @PostMapping
  @Operation(summary = "새로운 결제 생성", description = "새로운 결제를 생성합니다.")
  public ApiResponse<Void> createPayment(
      @Valid @RequestBody CreatePaymentRequest createPaymentRequest) {
    paymentService.createPayment(createPaymentRequest.toPaymentRequest());
    return ApiResponse.success();
  }

  // 2. 결제 성공
  @GetMapping("/resp/success")
  public ApiResponse<?> paymentSuccess(
      @RequestParam String paymentKey, @RequestParam String orderId, @RequestParam long amount) {
    paymentService.confirmPayment(paymentKey, UUID.fromString(orderId), amount);
    return ApiResponse.success();
  }

  // 3. 결제 실패
  @GetMapping("/resp/fail")
  public ApiResponse<?> paymentFail(
      String code, @RequestParam(required = false) String message, @RequestParam String orderId) {
    log.warn("[PAYMENT-FAIL] orderId={}, code={}, message={}", orderId, code, message);
    paymentService.failPayment(UUID.fromString(orderId), code);
    return ApiResponse.success();
  }

  // 4. 환불
  @PostMapping("/refund")
  public ApiResponse<Void> paymentRefund(@Valid @RequestBody RefundPaymentRequest request) {
    paymentService.refundPayment(request.toRefundRequest());
    return ApiResponse.success();
  }
}
