package com.tickatch.paymentservice.global.config;

import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Client 설정.
 *
 * <p>서비스 간 통신을 위한 Feign Client 설정을 제공한다.
 *
 * <ul>
 *   <li>로깅 레벨: BASIC
 *   <li>에러 디코더: FeignErrorDecoder (BusinessException 변환)
 *   <li>요청 인터셉터: 인증 헤더 전파
 *   <li>재시도: 100ms 간격으로 최대 3회
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Configuration
@EnableFeignClients(basePackages = "com.tickatch.paymentservice")
public class FeignConfig {

  /**
   * Feign 로깅 레벨.
   *
   * <ul>
   *   <li>NONE: 로깅 없음
   *   <li>BASIC: 요청 메서드, URL, 응답 상태 코드, 실행 시간
   *   <li>HEADERS: BASIC + 요청/응답 헤더
   *   <li>FULL: HEADERS + 요청/응답 본문
   * </ul>
   */
  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.BASIC;
  }

  /**
   * Feign 에러 디코더. HTTP 에러 응답을 BusinessException으로 변환한다.
   */
  @Bean
  public ErrorDecoder errorDecoder() {
    return new FeignErrorDecoder();
  }

  /**
   * Feign 요청 인터셉터. X-User-Id 등 인증 헤더를 다른 서비스로 전파한다.
   */
  @Bean
  public FeignRequestInterceptor feignRequestInterceptor() {
    return new FeignRequestInterceptor();
  }

  /**
   * Feign 재시도 설정. 100ms 간격으로 최대 3회 재시도한다.
   */
  @Bean
  public Retryer retryer() {
    return new Retryer.Default(100, 1000, 3);
  }
}