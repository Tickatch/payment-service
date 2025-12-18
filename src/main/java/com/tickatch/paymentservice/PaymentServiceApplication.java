package com.tickatch.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Boot 애플리케이션의 진입점 클래스.
 *
 * <p>이 클래스는 {@link SpringBootApplication}을 사용하여 Spring Boot 환경에서 애플리케이션을 실행합니다.
 *
 * @author 김형섭
 * @since 1.0.0
 */
@EnableFeignClients
@EnableAsync
@SpringBootApplication
public class PaymentServiceApplication {

  /**
   * 애플리케이션 실행 진입점.
   *
   * @param args 명령줄 인자
   */
  public static void main(String[] args) {
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}
