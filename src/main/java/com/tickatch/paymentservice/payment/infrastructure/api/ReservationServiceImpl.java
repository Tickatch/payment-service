package com.tickatch.paymentservice.payment.infrastructure.api;

import com.tickatch.paymentservice.payment.application.dto.PaymentResultRequest;
import com.tickatch.paymentservice.payment.domain.service.ReservationService;
import com.tickatch.paymentservice.payment.infrastructure.client.ReservationFeignClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

  private final ReservationFeignClient reservationFeignClient;

  @Override
  public void applyResult(String status, List<String> ids) {
    reservationFeignClient.applyPaymentResult(new PaymentResultRequest(status, ids));
  }
}
