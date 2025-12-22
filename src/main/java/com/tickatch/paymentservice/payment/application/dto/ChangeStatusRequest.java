package com.tickatch.paymentservice.payment.application.dto;

import java.util.List;

public record ChangeStatusRequest(List<String> reservationIds) {}
