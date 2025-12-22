package com.tickatch.paymentservice.global.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.github.tickatch.common.logging.MdcUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign Client 요청 인터셉터.
 *
 * <p>현재 요청의 인증 헤더와 추적 정보를 다른 서비스로 전파한다.
 *
 * <p>전파하는 헤더:
 *
 * <ul>
 *   <li>X-User-Id: 사용자 ID
 *   <li>X-Trace-Id: 커스텀 추적 ID (MdcFilter용)
 *   <li>X-B3-TraceId: B3 추적 ID (Brave/Zipkin용)
 *   <li>X-B3-SpanId: B3 스팬 ID (Brave/Zipkin용)
 *   <li>X-B3-Sampled: 샘플링 여부 (Brave/Zipkin용)
 * </ul>
 *
 * @author Tickatch
 * @since 1.0.0
 */
@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {

  // 커스텀 헤더
  private static final String HEADER_USER_ID = "X-User-Id";
  private static final String HEADER_USER_TYPE = "X-User-Type";
  private static final String HEADER_TRACE_ID = "X-Trace-Id";

  // B3 헤더 (Brave/Zipkin 표준)
  private static final String HEADER_B3_TRACE_ID = "X-B3-TraceId";
  private static final String HEADER_B3_SPAN_ID = "X-B3-SpanId";
  private static final String HEADER_B3_SAMPLED = "X-B3-Sampled";

  @Override
  public void apply(RequestTemplate template) {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      propagateHeader(request, template, HEADER_USER_ID);
      propagateHeader(request, template, HEADER_USER_TYPE);
    }

    // Brave가 MDC에 설정한 traceId/spanId 전파
    String traceId = MdcUtils.get("traceId");
    String spanId = MdcUtils.get("spanId");

    if (StringUtils.hasText(traceId)) {
      // 커스텀 헤더 (MdcFilter용)
      template.header(HEADER_TRACE_ID, traceId);

      // B3 헤더 (Brave가 인식)
      template.header(HEADER_B3_TRACE_ID, traceId);
      template.header(HEADER_B3_SAMPLED, "1");

      if (StringUtils.hasText(spanId)) {
        template.header(HEADER_B3_SPAN_ID, spanId);
      }

      log.info("헤더 전파: traceId={}, spanId={}", traceId, spanId);
    }
  }

  private void propagateHeader(
      HttpServletRequest request, RequestTemplate template, String headerName) {
    String headerValue = request.getHeader(headerName);
    if (StringUtils.hasText(headerValue)) {
      template.header(headerName, headerValue);
      log.debug("헤더 전파: {} = {}", headerName, headerValue);
    }
  }
}