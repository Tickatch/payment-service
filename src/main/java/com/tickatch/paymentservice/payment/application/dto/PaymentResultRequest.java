package com.tickatch.paymentservice.payment.application.dto;

import java.util.List;

public record PaymentResultRequest(String status, List<String> reservationIds) {

}
