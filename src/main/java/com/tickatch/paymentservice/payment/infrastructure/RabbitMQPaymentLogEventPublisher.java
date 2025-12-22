package com.tickatch.paymentservice.payment.infrastructure;

import static com.tickatch.paymentservice.payment.infrastructure.config.RabbitMQConfig.LOG_EXCHANGE;
import static com.tickatch.paymentservice.payment.infrastructure.config.RabbitMQConfig.ROUTING_KEY;

import com.tickatch.paymentservice.global.config.ActorExtractor;
import com.tickatch.paymentservice.global.config.ActorExtractor.ActorInfo;
import com.tickatch.paymentservice.payment.application.PaymentLogEventPublisher;
import com.tickatch.paymentservice.payment.domain.PaymentMethod;
import com.tickatch.paymentservice.payment.domain.exception.PaymentErrorCode;
import com.tickatch.paymentservice.payment.domain.exception.PaymentException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQPaymentLogEventPublisher implements PaymentLogEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publish(UUID paymentId, PaymentMethod method, int retryCount, String actionType) {
    ActorInfo actor = ActorExtractor.extract();

    PaymentLogEvent logEvent =
        new PaymentLogEvent(
            UUID.randomUUID(),
            paymentId,
            method.name(),
            retryCount,
            actionType,
            actor.actorType(),
            actor.actorUserId(),
            LocalDateTime.now());

    publishEvent(logEvent);
  }

  private void publishEvent(PaymentLogEvent event) {
    log.info("{} 로그 이벤트 발행 시작", event.actionType());

    try {
      rabbitTemplate.convertAndSend(LOG_EXCHANGE, ROUTING_KEY, event);
      log.info(
          "{} 이벤트 발행 완료: exchange={}, routingKey={}",
          event.actionType(),
          LOG_EXCHANGE,
          ROUTING_KEY);
    } catch (Exception e) {
      log.error(
          "{} 이벤트 발행 실패: exchange={}, routingKey={}, event={}",
          event.actionType(),
          LOG_EXCHANGE,
          ROUTING_KEY,
          event,
          e);
      throw new PaymentException(PaymentErrorCode.PAYMENT_EVENT_PUBLISH_FAILED, e);
    }
  }
}
