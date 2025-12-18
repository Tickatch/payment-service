package com.tickatch.paymentservice.payment.application.dto;

import com.tickatch.paymentservice.payment.domain.RefundReason;
import java.util.List;

public record RefundRequest(RefundReason reason, List<String> reservationIds) {}
