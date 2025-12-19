package com.tickatch.paymentservice.payment.infrastructure;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentLogEvent(
    UUID eventId, // 발행 서비스에서 생성하는 이벤트 ID
    UUID paymentId,
    String method, // TOSS_CARD 등 (없으면 null)
    int retryCount,
    String actionType, // REQUESTED / PROCESSING / SUCCESS / FAIL / CANCEL / REFUND / ...
    String actorType,
    UUID actorUserId,
    LocalDateTime occurredAt) {}
