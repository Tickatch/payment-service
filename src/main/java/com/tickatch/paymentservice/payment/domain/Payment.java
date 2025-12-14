package com.tickatch.paymentservice.payment.domain;

import com.tickatch.paymentservice.global.domain.AbstractAuditEntity;
import com.tickatch.paymentservice.payment.domain.dto.PaymentReservationInfo;
import com.tickatch.paymentservice.payment.domain.exception.PaymentErrorCode;
import com.tickatch.paymentservice.payment.domain.exception.PaymentException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Table(name = "p_payment")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends AbstractAuditEntity {

  // 결제 id
  @EmbeddedId private PaymentId id;

  // 결제 상태
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  // 결제 금액
  @Column(nullable = false)
  private Long totalPrice;

  // 결제 수단
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentMethod method;

  // 결제 세부 정보
  @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
  private PaymentDetail detail;

  // 결제 식별용 uuid(toss)
  @Column(nullable = false, updatable = false)
  private UUID orderId;

  // 재시도 횟수
  @Column private int retryCount = 0;

  //  @Column
  //  private String log;

  // 취소 이유
  @Enumerated(EnumType.STRING)
  private CancelReason cancelReason;

  // 결제 승인 시각
  @Column private LocalDateTime approvedAt;

  // 결제 취소 시각
  @Column private LocalDateTime canceledAt;

  // 환불 시각
  @Column private LocalDateTime refundedAt;

  // 결제에 대한 예매 목록
  @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
  List<PaymentReservationLink> links = new ArrayList<>();

  // =================================

  // 생성
  @Builder(access = AccessLevel.PRIVATE)
  public Payment(PaymentMethod method, PaymentStatus status, Long totalPrice, UUID orderId) {
    this.id = PaymentId.of();
    this.method = method;
    this.status = status;
    this.totalPrice = totalPrice;
    this.orderId = orderId;
  }

  public static Payment create(List<PaymentReservationInfo> infos, PaymentMethod method) {
    Long totalPrice = calculateTotalPrice(infos);

    Payment payment =
        Payment.builder()
            .method(method)
            .status(PaymentStatus.REQUESTED)
            .totalPrice(totalPrice)
            .orderId(UUID.randomUUID())
            .build();

    payment.addReservationLinks(infos);

    payment.validateLinks();
    payment.validatePrice();
    payment.validateMethod();
    payment.validateDuplicateReservationIds();

    return payment;
  }

  // 예매-결제 링크 생성
  private void addReservationLinks(List<PaymentReservationInfo> infos) {
    for (PaymentReservationInfo info : infos) {
      PaymentReservationLink link =
          new PaymentReservationLink(this, info.reservationId(), info.price(), LinkStatus.PENDING);
      this.links.add(link);
      log.info("Added link, current size: {}", this.links.size());
    }
    log.info("After adding links, size: {}", this.links.size());
  }

  // 결제 총 금액 계산
  private static Long calculateTotalPrice(List<PaymentReservationInfo> infos) {
    return infos.stream().mapToLong(PaymentReservationInfo::price).sum();
  }

  // ==================================

  // 상태 관련

  // 1. 결제 성공으로 상태 변경
  // 이전 상태: 결제 처리중
  public void markSuccess() {
    if (this.status != PaymentStatus.PROCESSING) {
      throw new PaymentException(PaymentErrorCode.INVALID_STATUS_FOR_SUCCESS);
    }
    this.status = PaymentStatus.SUCCESS;
    this.approvedAt = LocalDateTime.now();
  }

  // 2. 결제 취소로 상태 변경
  // 예매 취소 시 자동 결제 취소 / 사용자 취소 시 결제 취소
  public void cancel(CancelReason reason) {
    if (this.status != PaymentStatus.SUCCESS) {
      throw new PaymentException(PaymentErrorCode.INVALID_STATUS_FOR_CANCEL);
    }
    this.status = PaymentStatus.CANCEL;
    this.cancelReason = reason;
    this.canceledAt = LocalDateTime.now();
  }

  // 3. 결제 실패로 상태 변경
  // 이전 상태: 결제 처리중
  public void markFail() {
    if (this.status != PaymentStatus.PROCESSING) {
      throw new PaymentException(PaymentErrorCode.INVALID_STATUS_FOR_FAIL);
    }
    this.status = PaymentStatus.FAIL;
    this.retryCount += 1;
  }

  // 4. 환불로 상태 변경
  // 이전 상태: 결제 성공
  public void refund() {
    if (this.status != PaymentStatus.SUCCESS) {
      throw new PaymentException(PaymentErrorCode.INVALID_STATUS_FOR_REFUND);
    }
    this.status = PaymentStatus.REFUND;
    this.refundedAt = LocalDateTime.now();
  }

  // 5. 환불 실패로 상태 변경
  // 이전 상태: 결제 성공
  public void refundFail() {
    if (this.status != PaymentStatus.SUCCESS) {
      throw new PaymentException(PaymentErrorCode.INVALID_STATUS_FOR_REFUND_FAIL);
    }
    this.status = PaymentStatus.REFUND_FAIL;
  }

  // 6. 결제 시간 만료
  // 이전 상태: 결제 요청, 결제중, 결제 실패
  public void expire() {
    if (this.status != PaymentStatus.REQUESTED
        && this.status != PaymentStatus.PROCESSING
        && this.status != PaymentStatus.FAIL) {
      throw new PaymentException(PaymentErrorCode.INVALID_STATUS_FOR_EXPIRED);
    }

    this.status = PaymentStatus.EXPIRED;
  }

  // 7. 결제 처리중으로 상태 변경
  // 결제 요청이 들어오고, 외부 api 호출되면 결제 처리중으로 변경
  // 이전 상태: 결제 요청
  public void markProcessing() {
    if (this.status != PaymentStatus.REQUESTED) {
      throw new PaymentException(PaymentErrorCode.INVALID_STATUS_FOR_PROCESSING);
    }
    this.status = PaymentStatus.PROCESSING;
  }

  // ==================================

  // 검증

  // 1. 예매-결제 링크 존재 검증
  private void validateLinks() {
    if (this.links == null || this.links.isEmpty()) {
      throw new PaymentException(PaymentErrorCode.NO_RESERVATION_LINK);
    }
  }

  // 2. 결제 금액 검증
  private void validatePrice() {
    if (this.totalPrice == null || this.totalPrice <= 0) {
      throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_PRICE);
    }
  }

  // 3. 결제 수단 검증
  private void validateMethod() {
    if (this.method == null) {
      throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_METHOD);
    }
  }

  // 4. 중복 결제 검증
  private void validateDuplicateReservationIds() {
    long uniqueCount =
        this.links.stream().map(PaymentReservationLink::getReservationId).distinct().count();

    if (uniqueCount != this.links.size()) {
      throw new PaymentException(PaymentErrorCode.DUPLICATE_RESERVATION_ID);
    }
  }

  // detail 세팅하는 메서드
  public void assignDetail(PaymentDetail detail) {
    this.detail = detail;
  }

  // 연관 예매 조회 링크
  public List<String> getReservationIds() {
    return this.links.stream().map(PaymentReservationLink::getReservationId).toList();
  }
}
