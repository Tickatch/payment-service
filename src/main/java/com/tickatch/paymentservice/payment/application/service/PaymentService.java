package com.tickatch.paymentservice.payment.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickatch.paymentservice.payment.application.dto.PaymentRequest;
import com.tickatch.paymentservice.payment.application.dto.RefundRequest;
import com.tickatch.paymentservice.payment.domain.Payment;
import com.tickatch.paymentservice.payment.domain.PaymentDetail;
import com.tickatch.paymentservice.payment.domain.PaymentMethod;
import com.tickatch.paymentservice.payment.domain.PaymentStatus;
import com.tickatch.paymentservice.payment.domain.RefundReason;
import com.tickatch.paymentservice.payment.domain.TossCardDetail;
import com.tickatch.paymentservice.payment.domain.dto.PaymentReservationInfo;
import com.tickatch.paymentservice.payment.domain.exception.PaymentErrorCode;
import com.tickatch.paymentservice.payment.domain.exception.PaymentException;
import com.tickatch.paymentservice.payment.domain.repository.PaymentRepository;
import com.tickatch.paymentservice.payment.domain.service.ReservationService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private final ReservationService reservationService;

  @Value("${toss.secret-key}")
  private String secretKey;

  @Value("${app.base-url}")
  private String baseUrl;

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
                + "\"successUrl\":\"%s/api/v1/payments/resp/success\", "
                + "\"failUrl\":\"%s/api/v1/payments/resp/fail\"}",
            totalPrice, orderId, baseUrl, baseUrl);

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

    String paymentKey = paymentKeyNode.asText();
    return paymentKey;
  }

  // 결제 승인 처리
  @Transactional
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

        // 결제 성공 상태로 변경
        payment.markSuccess();

        // 결제-예매 링크 확정
        payment.confirmReservationLinks();

        // 예매 쪽에 결제 성공 알리기
        //        reservationService.applyResult("SUCCESS", payment.getReservationIds());

      } else {
        // 실패 처리
        payment.markFail();

        // 예매 쪽에 결제 실패 알리기
        //        reservationService.applyResult("FAIL", payment.getReservationIds());
      }

      // DB 저장
      paymentRepository.save(payment);

    } catch (Exception e) {
      log.error("[TOSS-CONFIRM-ERROR] 결제 승인 중 오류 발생", e);
      throw new PaymentException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED, e);
    }
  }

  // 결제 실패 처리 : 사용자 취소로 인한 실패, 그 이외의 이유로 인한 실패
  @Transactional
  public void failPayment(UUID orderId, String code) {

    Payment payment =
        paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

    // 실패 처리할 수 없는 경우
    if (!payment.getStatus().canFail()) {
      log.warn("invalid fail transition. status={}", payment.getStatus());
      return;
    }

    // 사용자 취소로 인한 결제 실패
    if ("PAY_PROCESS_CANCELED".equals(code)) {
      // 결제 상태 cancel로 변경
      payment.cancel(RefundReason.CUSTOMER_CANCEL);

      // 예매 쪽에 전달
      reservationService.applyResult("CANCEL", payment.getReservationIds());
      return;
    }

    // 결제 실패로 상태 변경
    payment.markFail();

    // 예매 쪽에 결제 실패 알리기
    reservationService.applyResult("FAIL", payment.getReservationIds());
  }

  // 환불: 예매 취소 시 환불, 상품 삭제 시 환불
  @Transactional
  public void refundPayment(RefundRequest request) {

    List<String> reservationIds = request.reservationIds();
    RefundReason reason = request.reason();

    // 예매 id들이 속하는 payment 가져오기
    List<Payment> payments = paymentRepository.findPaymentsByReservationIds(reservationIds);

    // 결제가 없을 때
    if (payments.isEmpty()) {
      throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
    }

    // 예매 id가 서로 다른 결제에 속할 때
    if (payments.size() != 1) {
      throw new PaymentException(PaymentErrorCode.MULTIPLE_PAYMENT_FOUND);
    }

    Payment payment = payments.get(0);

    // 이미 환불된 경우
    if (payment.getStatus() == PaymentStatus.REFUND) {
      log.info("[PAYMENT-REFUND] already refunded. paymentId={}", payment.getId());
      return;
    }

    // payment가 모든 예매 id 포함하고 있는지 확인
    Set<String> paymentReservationIds = new HashSet<>(payment.getReservationIds());

    if (!paymentReservationIds.containsAll(reservationIds)) {
      throw new PaymentException(PaymentErrorCode.INVALID_RESERVATION_FOR_PAYMENT);
    }

    // paymentDetail에서 payment key 가져오기
    PaymentDetail detail = payment.getDetail();

    // payment key 찾을 수 없을 때
    if (detail == null || detail.getPaymentKey() == null) {
      throw new PaymentException(PaymentErrorCode.PAYMENT_KEY_NOT_FOUND);
    }

    String paymentKey = detail.getPaymentKey();

    try {
      String auth = secretKey.trim() + ":";
      String encodedAuth =
          Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel"))
              .header("Authorization", "Basic " + encodedAuth)
              .header("Content-Type", "application/json")
              .POST(
                  HttpRequest.BodyPublishers.ofString(
                      "{\"cancelReason\":\"" + reason.name() + "\"}"))
              .build();

      HttpResponse<String> response =
          HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

      JsonNode jsonNode = objectMapper.readTree(response.body());

      if (response.statusCode() == 200 && "CANCELED".equals(jsonNode.path("status").asText())) {

        // 환불 성공으로 상태 변경
        log.info("[SUCCESS REFUND] refund paymentId={}", payment.getId());
        payment.refund(reason);
      } else {
        String errorMessage = jsonNode.path("message").asText("refund failed");

        // 환불 실패로 상태 변경
        payment.refundFail(reason);
        log.error("[PAYMENT-REFUND-FAIL] {}", errorMessage);
      }

    } catch (Exception e) {
      payment.refundFail(reason);
      log.error("[PAYMENT-REFUND-ERROR]", e);
      throw new PaymentException(PaymentErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
