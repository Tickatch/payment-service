package com.tickatch.paymentservice.global.config;

import io.github.tickatch.common.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/*
* 현재 요청을 보낸 주체(Actor)가 누구인지”를
Spring Security Context에서 안전하게 꺼내는 공통 유틸
* */

public class AuthExtractor {

  public static AuthInfo extract() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
      return AuthInfo.anonymous();
    }

    Object principal = auth.getPrincipal();

    if (principal instanceof AuthenticatedUser user) {
      return AuthInfo.user(user.getUserType().name(), user.getUserId());
    }

    return AuthInfo.anonymous();
  }

  public record AuthInfo(String userType, String userId) {

    public boolean isAdmin() {
      return "ADMIN".equals(userType);
    }

    public static AuthInfo anonymous() {
      return new AuthInfo("ANONYMOUS", null);
    }

    public static AuthInfo user(String userType, String userId) {
      return new AuthInfo(userType, userId);
    }
  }
}
