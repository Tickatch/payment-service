package com.tickatch.paymentservice.payment.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickatch.paymentservice.payment.application.dto.PaymentRequest;
import com.tickatch.paymentservice.payment.domain.Payment;
import com.tickatch.paymentservice.payment.domain.PaymentMethod;
import com.tickatch.paymentservice.payment.domain.TossCardDetail;
import com.tickatch.paymentservice.payment.domain.dto.PaymentReservationInfo;
import com.tickatch.paymentservice.payment.domain.exception.PaymentErrorCode;
import com.tickatch.paymentservice.payment.domain.exception.PaymentException;
import com.tickatch.paymentservice.payment.domain.repository.PaymentRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final ObjectMapper objectMapper;
  private final PaymentRepository paymentRepository;

  @Value("${toss.secret-key}")
  private String secretKey;

  // 1. 결제 생성

  // 결제 생성
  //  @Async
  @Transactional
  public void createPayment(PaymentRequest paymentRequest) {

    List<PaymentReservationInfo> infos =
        paymentRequest.payments().stream()
            .map(p -> new PaymentReservationInfo(p.reservationId(), p.price()))
            .toList();

    // 결제 엔티티 생성 후 저장(id 생성)
    Payment payment = Payment.create(infos, PaymentMethod.TOSS_CARD);
    payment.markProcessing();
    paymentRepository.save(payment);

    try {
      // 결제 키 발급
      String paymentKey = createPaymentKey(payment.getOrderId(), payment.getTotalPrice());
      log.info("Payment created: {}", paymentKey);
    } catch (Exception e) {
      // 결제 키 발급 실패
      throw new PaymentException(PaymentErrorCode.PAYMENT_KEY_GENERATION_FAILED);
    }
  }

  // 결제 키 발급
  private String createPaymentKey(UUID orderId, long totalPrice) throws Exception {

    String url = "https://api.tosspayments.com/v1/payments";
    String auth = secretKey.trim() + ":";
    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

    String bodyJson =
        String.format(
            "{\"method\":\"CARD\", \"amount\":%d, \"orderId\":\"%s\", \"orderName\":\"테스트 결제\", "
                + "\"successUrl\":\"http://localhost:8081/api/v1/payments/resp/success\", "
                + "\"failUrl\":\"http://localhost:8081/api/v1/payments/resp/fail\"}",
            totalPrice, orderId);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Basic " + encodedAuth)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
            .build();

    HttpResponse<String> response =
        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

    JsonNode jsonNode = objectMapper.readTree(response.body());

    System.out.println("결제 UI : " + jsonNode.get("checkout").get("url").asText());

    // http 상태 체크
    if (response.statusCode() != 200) {
      log.error("[TOSS-PAYMENT-ERROR] status={}, body={}", response.statusCode(), response.body());
      throw new PaymentException(PaymentErrorCode.PAYMENT_KEY_GENERATION_FAILED);
    }

    // paymentKey 존재 여부 확인
    JsonNode paymentKeyNode = jsonNode.get("paymentKey");
    if (paymentKeyNode == null || paymentKeyNode.asText().isBlank()) {
      log.error("[TOSS-PAYMENT-ERROR] paymentKey missing, body={}", response.body());
      throw new PaymentException(PaymentErrorCode.PAYMENT_KEY_GENERATION_FAILED);
    }

    //    return jsonNode.get("paymentKey").asText();

    String paymentKey = paymentKeyNode.asText();
    log.info(
        "Payment created: {}, checkout URL: {}",
        paymentKey,
        jsonNode.path("checkout").path("url").asText());
    return paymentKey;
  }

  // 결제 승인 처리
  public void confirmPayment(String paymentKey, UUID orderId, long totalPrice) {
    try {
      String url = "https://api.tosspayments.com/v1/payments/confirm";
      String auth = secretKey.trim() + ":";
      String encodedAuth =
          Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

      String bodyJson =
          objectMapper.writeValueAsString(
              Map.of(
                  "paymentKey", paymentKey,
                  "orderId", orderId.toString(),
                  "amount", totalPrice));

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .header("Authorization", "Basic " + encodedAuth)
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
              .build();

      HttpResponse<String> response =
          HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

      log.info("[TOSS-CONFIRM] status={}, body={}", response.statusCode(), response.body());

      JsonNode jsonNode = objectMapper.readTree(response.body());

      Payment payment =
          paymentRepository
              .findByOrderId(orderId)
              .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

      // 승인 상태 확인
      if (response.statusCode() == 200 && "DONE".equals(jsonNode.path("status").asText())) {
        TossCardDetail detail = TossCardDetail.create(payment, paymentKey);
        payment.markSuccess();
      } else {
        // 실패 처리
        payment.markFail();
      }

      // DB 저장
      paymentRepository.save(payment);

    } catch (Exception e) {
      log.error("[TOSS-CONFIRM-ERROR] 결제 승인 중 오류 발생", e);
      throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED, e);
    }
  }
}
