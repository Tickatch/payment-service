package com.tickatch.paymentservice.payment.infrastructure;

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
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQPaymentLogEventPublisher implements PaymentLogEventPublisher {

  private static final String ROUTING_KEY = "payment.log";

  private final RabbitTemplate rabbitTemplate;

  @Value("${messaging.exchange.payment:tickatch.payment}")
  private String paymentExchange;

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
      rabbitTemplate.convertAndSend(paymentExchange, ROUTING_KEY, event);
      log.info(
          "{} 이벤트 발행 완료: exchange={}, routingKey={}",
          event.actionType(),
          paymentExchange,
          ROUTING_KEY);
    } catch (AmqpException e) {
      log.error(
          "{} 이벤트 발행 실패: exchange={}, routingKey={}, event={}",
          event.actionType(),
          paymentExchange,
          ROUTING_KEY,
          event,
          e);
      throw new PaymentException(PaymentErrorCode.PAYMENT_EVENT_PUBLISH_FAILED, e);
    }
  }
}
