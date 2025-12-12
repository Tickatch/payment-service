package com.tickatch.paymentservice.payment.presentation.api;

import com.tickatch.paymentservice.payment.application.service.PaymentService;
import com.tickatch.paymentservice.payment.presentation.dto.CreatePaymentRequest;
import io.github.tickatch.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
