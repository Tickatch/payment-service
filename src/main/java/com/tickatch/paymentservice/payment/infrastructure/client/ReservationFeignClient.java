package com.tickatch.paymentservice.payment.infrastructure.client;

import com.tickatch.paymentservice.payment.application.dto.ChangeStatusRequest;
import com.tickatch.paymentservice.payment.application.dto.PaymentResultRequest;
import io.github.tickatch.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reservation-service")
public interface ReservationFeignClient {

  // 예매 결과 전송
  @PatchMapping("/api/v1/reservations/payment-result")
  ApiResponse<Void> applyPaymentResult(@RequestBody PaymentResultRequest request);

  // 결제 생성 시 예매 상태 변경
  @PatchMapping("/api/v1/reservations/pending-payment")
  ApiResponse<Void> changeReservationStatus(@RequestBody ChangeStatusRequest request);
}
