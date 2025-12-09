package com.tickatch.paymentservice.global.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 엔티티의 기본 시간 감사를 제공하는 추상 클래스.
 *
 * <p>생성시간(createdAt), 수정시간(updatedAt) 필드를 제공하며, Spring Data JPA의 감사(audit) 기능을 활성화합니다.
 *
 * @author 김형섭
 * @since 1.0.0
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractTimeEntity {
  /* 등록 일시 */
  @Column(name = "created_at", nullable = false, updatable = false)
  @CreatedDate
  private LocalDateTime createdAt;

  /* 수정 일시 */
  @Column(name = "updated_at", nullable = false)
  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AbstractTimeEntity entity)) {
      return false;
    }

    return Objects.equals(createdAt, entity.createdAt)
        && Objects.equals(updatedAt, entity.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdAt, updatedAt);
  }
}
