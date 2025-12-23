# Tickatch Payment Service

티켓 예매 플랫폼 Tickatch의 결제 관리 마이크로서비스입니다.

## 프로젝트 소개

Payment Service는 결제의 전 과정의 상태 흐름을 관리하며, 예매와의 동기화를 통해 안정적인 결제 처리를 담당합니다.

## 기술 스택

| 분류            | 기술                       |
|---------------|--------------------------|
| Framework     | Spring Boot 3.x          |
| Language      | Java 21                  |
| Database      | PostgreSQL               |
| Messaging     | RabbitMQ                 |
| Query         | QueryDSL / JPA           |
| Communication | OpenFeign / RestTemplate |
| Security      | Spring Security          |

## 아키텍처

### 시스템 구성

```
┌────────────────────────────────────────────────────────────┐
│                          Tickatch Platform                 │
├──────────────┬──────────────┬──────────────┬───────────────┤
│     User     │     Auth     │   Product    │    ArtHall    │
│   Service    │   Service    │   Service    │    Service    │
├──────────────┼──────────────┼──────────────┼───────────────┤
│ Reservation  │    Ticket    │ Reservation  │    Payment    │
│   Service    │   Service    │     Seat     │    Service    │
│              │              │   Service    │               │
├──────────────┴──────────────┴──────────────┴───────────────┤
│    Notification Service     │         Log Service          │
└────────┬────────────────────┴──────────┬───────────────────┘
         │                               │
         └────────────────────┬──────────┘
                              │
                          RabbitMQ
```

### 레이어 구조

```
src/main/java/com/tickatch/paymentservice
├── PaymentServiceApplication.java
├── global
│   ├── config
│   │   ├── ActorExtractor.java
│   │   ├── AuditorAwareImpl.java
│   │   ├── AuthExtractor.java
│   │   ├── FeignConfig.java
│   │   ├── FeignErrorDecoder.java
│   │   ├── FeignRequestInterceptor.java
│   │   └── SecurityConfig.java
│   └── domain
│       ├── AbstractAuditEntity.java
│       └── AbstractTimeEntity.java
└── payment
    ├── application
    │   ├── PaymentActionType.java
    │   ├── PaymentLogEventPublisher.java
    │   ├── dto
    │   │   ├── ChangeStatusRequest.java
    │   │   ├── PaymentRequest.java
    │   │   ├── PaymentResultRequest.java
    │   │   └── RefundRequest.java
    │   └── service
    │       └── PaymentService.java
    ├── domain
    │   ├── LinkStatus.java
    │   ├── Payment.java
    │   ├── PaymentDetail.java
    │   ├── PaymentId.java
    │   ├── PaymentMethod.java
    │   ├── PaymentReservationLink.java
    │   ├── PaymentStatus.java
    │   ├── RefundReason.java
    │   ├── TossCardDetail.java
    │   ├── dto
    │   │   └── PaymentReservationInfo.java
    │   ├── exception
    │   │   ├── PaymentErrorCode.java
    │   │   └── PaymentException.java
    │   ├── repository
    │   │   └── PaymentRepository.java
    │   └── service
    │       └── ReservationService.java
    ├── infrastructure
    │   ├── PaymentLogEvent.java
    │   ├── RabbitMQPaymentLogEventPublisher.java
    │   ├── api
    │   │   └── ReservationServiceImpl.java
    │   ├── client
    │   │   └── ReservationFeignClient.java
    │   └── config
    │       └── RabbitMQConfig.java
    └── presentation
        ├── api
        │   └── PaymentApi.java
        ├── config
        │   └── JPAConfig.java
        └── dto
            ├── CreatePaymentRequest.java
            └── RefundPaymentRequest.java
```

## 도메인 모델

### Payment (Aggregate Root)

결제의 전체 라이프사이클을 관리하는 핵심 엔티티입니다.

```
Payment
├── 기본 정보
│   ├── paymentId          # 결제 ID
│   ├── totalPrice         # 결제 금액
│   └── method             # 결제 수단
│
├── 결제 세부 정보
│   └── PaymentDetail
│
├── 예매 내역
│   └── PaymentReservationLink 
│
└── 상태
    ├── PaymentStatus    
    └── LinkStatus
```

### Value Objects

| VO            | 설명         | 주요 필드                                                                      |
|---------------|------------|----------------------------------------------------------------------------|
| PaymentStatus | 결제 상태      | REQUESTED, SUCCESS, CANCEL, FAIL, REFUND, REFUND_FAIL, EXPIRED, PROCESSING |
| LinkStatus    | 결제-예매 간 상태 | PENDING, CONFIRMED                                                         |
| PaymentMethod | 결제 방법      | TOSS_CARD                                                                  |
| RefundReason  | 환불 이유      | CUSTOMER_CANCEL, PRODUCT_CANCEL                                            |

### PaymentDetail

결제 상세 정보를 관리하는 엔티티입니다. Payment에 종속됩니다.

| 필드         | 설명          |
|------------|-------------|
| id         | 상세 정보 식별 id |
| paymentId  | 결제 id       |
| paymentKey | 결제키         |

### PaymentReservationLink

결제와 관련된 예매 정보들을 관리하는 엔티티입니다. Payment에 종속됩니다.

| 필드            | 설명            |
|---------------|---------------|
| id            | 예매-결제 간 식별 id |
| reservationId | 예매 id         |
| paymentId     | 결제 id         |
| status        | 예매-결제 간 상태    |
| price         | 가격            |

## 결제 상태(PaymentStatus)

### 상태 종류

| 상태            | 설명                  | 최종 상태 |
|---------------|---------------------|:-----:|
| `REQUESTED`   | 결제 요청 - 결제 요청 생성    |   ❌   |
| `PROCESSING`  | 결제 처리 중 - PG사 처리 진행 |   ❌   |
| `SUCCESS`     | 결제 성공 - 결제 승인 완료    |   ✅   |
| `CANCEL`      | 사용자 결제 취소           |   ✅   |
| `FAIL`        | 결제 실패 - 사용자 취소 외 실패 |   ✅   |
| `EXPIRED`     | 결제 시간 만료            |   ✅   |
| `REFUND`      | 환불 성공 - 환불 처리 완료    |   ✅   |
| `REFUND_FAIL` | 환불 실패 - 환불 처리 실패    |   ✅   |

### 상태 전이 다이어그램

```
                                      ┌──────────┐
                                      │ EXPIRED  │ (최종)
                                      └──────────┘
                                           ↑
                                           │ (시간 만료)
                                           │
┌───────────┐     ┌─────────────┐     ┌──────────┐
│ REQUESTED │────→│ PROCESSING  │────→│ SUCCESS  │ (최종)
└───────────┘     └─────────────┘     └──────────┘
                         │                   │
                         │                   ├──────→ ┌──────────┐
                         │                   │        │  REFUND  │ (최종)
                         │                   │        └──────────┘
                         │                   │
                         │                   └──────→ ┌──────────────┐
                         │                            │ REFUND_FAIL  │ (최종)
                         │                            └──────────────┘
                         │
                         ├──────→ ┌──────────┐
                         │        │  CANCEL  │ (최종)
                         │        └──────────┘
                         │
                         └──────→ ┌──────────┐
                                  │   FAIL   │ (최종)
                                  └──────────┘
```

## 주요 기능

### 결제 관리

- 결제 생성(REQUESTED 상태로 시작)
    - 결제 생성 시 예매 상태를 결제 진행중으로 변경
- 결제 성공/실패
    - 결과에 맞는 상태 변경
    - 결제 결과를 예매에 전송하여 결제-예매 간 상태 동기화
- 환불
    - 예매 취소 시 환불
    - 상품 삭제 시 환불

### 결제 상태 관리

- 상태 관리
    - 결제 상태 전이 관리 : REQUESTED → PROCESSING → SUCCESS / CANCEL / FAIL

## API 명세

Base URL: `/api/v1/payments`

### 결제 요청

| Method | Endpoint | 설명            | 인증 |
|--------|----------|---------------|:--:|
| POST   | `/`      | 결제 생성 (결제 요청) | ✅  |

### 결제 결과 처리

| Method | Endpoint        | 설명                | 인증 |
|--------|-----------------|-------------------|:--:|
| GET    | `/resp/success` | 결제 성공 처리 (PG사 콜백) | ❌  |
| GET    | `/resp/fail`    | 결제 실패 처리 (PG사 콜백) | ❌  |

### 환불

| Method | Endpoint  | 설명    | 인증 |
|--------|-----------|-------|:--:|
| POST   | `/refund` | 환불 요청 | ✅  |

### Request DTOs

#### CreatePaymentRequest (결제 생성)

| 구분        | 필드              | 타입                  | 필수 | 설명                     |
|-----------|-----------------|---------------------|:--:|------------------------|
| **주문 정보** | orderName       | String              | ✅  | 주문명 (예: "콘서트 티켓 2매")   |
| **결제 항목** | payments        | List\<PaymentItem\> | ✅  | 결제 대상 예매 목록 (다건 결제 지원) |
|           | └ reservationId | String              | ✅  | 예매 ID                  |
|           | └ price         | Long                | ✅  | 결제 금액 (원)              |

#### RefundPaymentRequest (환불 요청)

| 구분        | 필드             | 타입             | 필수 | 설명                          |
|-----------|----------------|----------------|:--:|-----------------------------|
| **환불 정보** | reason         | String         | ✅  | 환불 사유 (RefundReason enum 값) |
| **예매 정보** | reservationIds | List\<String\> | ✅  | 환불할 예매 ID 리스트 (다건 환불 지원)    |

## 이벤트

### 발행 이벤트 (Producer)

| 이벤트             | Routing Key   | 대상 서비스      | 설명             |
|-----------------|---------------|-------------|----------------|
| PaymentLogEvent | `payment.log` | Log Service | 결제 관련 로그 정보 발송 |

## 외부 연동

### Feign Client

| 서비스               | 용도                  |
|-------------------|---------------------|
| ReservationClient | 결제 결과 전송 및 결제 생성 알림 |

## 실행 방법

### 환경 변수

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tickatch
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  rabbitmq:
    host: localhost
    port: 5672
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}

toss:
  payments:
    secret-key: ${TOSS_SECRET_KEY}
```

### 실행

```bash
./gradlew bootRun
```

### 테스트

```bash
./gradlew test
```

### 코드 품질 검사

```bash
./gradlew spotlessApply spotbugsMain spotbugsTest
```

## 데이터 모델

### ERD

```
┌─────────────────────────────────────────────────────────────────┐
│                         p_payment                               │
├─────────────────────────────────────────────────────────────────┤
│ id                    UUID PK                                   │
│ order_id              UUID NOT NULL                             │
│ order_name            VARCHAR(255) NOT NULL                     │
│ total_price           BIGINT NOT NULL                           │
│ method                VARCHAR NOT NULL (TOSS_CARD)              │
│ status                VARCHAR NOT NULL                          │
│ retry_count           INTEGER                                   │
│ approved_at           TIMESTAMP                                 │
│ canceled_at           TIMESTAMP                                 │
│ refunded_at           TIMESTAMP                                 │
│ refund_reason         VARCHAR (CUSTOMER_CANCEL/PRODUCT_CANCEL)  │
│ created_at            TIMESTAMP NOT NULL                        │
│ created_by            VARCHAR(255) NOT NULL                     │
│ updated_at            TIMESTAMP NOT NULL                        │
│ updated_by            VARCHAR(255) NOT NULL                     │
│ deleted_at            TIMESTAMP                                 │
│ deleted_by            VARCHAR(255)                              │
└──────────────┬──────────────────────────────┬───────────────────┘
               │ 1:1                          │ 1:N
               ↓                              ↓
┌──────────────────────────────┐  ┌──────────────────────────────┐
│    p_payment_detail          │  │   p_payment_reservation      │
├──────────────────────────────┤  ├──────────────────────────────┤
│ id           UUID PK         │  │ id           BIGINT PK       │
│ payment_id   UUID FK UNIQUE  │  │ payment_id   UUID FK         │
│ payment_key  VARCHAR(255)    │  │ reservation_id VARCHAR(255)  │
│ payment_type VARCHAR(31)     │  │ price        BIGINT          │
└──────────────────────────────┘  │ status       VARCHAR         │
                                  └──────────────────────────────┘
```

## 관련 서비스/프로젝트

| 서비스                 | 역할    |
|---------------------|-------|
| Reservation Service | 예매 관리 |
| Ticket Service      | 티켓 관리 |
| Product Service     | 상품 관리 |
| Log Service         | 로그 관리 |

---

© 2025 Tickatch Team