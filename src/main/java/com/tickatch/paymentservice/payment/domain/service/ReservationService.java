package com.tickatch.paymentservice.payment.domain.service;

import java.util.List;

public interface ReservationService {

  void applyResult(String status, List<String> reservationIds);

  void changeStatus(List<String> reservationIds);
}
