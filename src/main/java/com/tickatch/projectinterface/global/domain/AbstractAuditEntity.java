package com.tickatch.projectinterface.global.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

/**
 * 엔티티의 전체 감사를 제공하는 추상 클래스.
 *
 * <p>생성자(createdBy, createdAt), 수정자(updatedBy, updatedAt), 삭제자(deletedBy, deletedAt) 필드를 제공하며,
 * Spring Data JPA의 감사(audit) 기능을 활성화합니다.
 *
 * @author 김형섭
 * @since 1.0.0
 */
@Getter
@MappedSuperclass
public abstract class AbstractAuditEntity extends AbstractTimeEntity {
  /* 등록자 계정 */
  @Column(name = "created_by", nullable = false)
  @CreatedBy
  private String createdBy;

  /* 수정자 계정 */
  @Column(name = "updated_by", nullable = false)
  @LastModifiedBy
  private String updatedBy;

  /* 삭제자 계정 */
  @Column(name = "deleted_by")
  private String deletedBy;

  /* 삭제 일시 */
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  /**
   * 엔티티를 생성할 때 호출하여 createdBy를 설정합니다.
   *
   * @param username 생성자 계정
   */
  protected void createBy(final String username) {
    this.createdBy = username;
  }

  /**
   * 엔티티를 수정할 때 호출하여 updateBy를 설정합니다.
   *
   * @param username 수정자 계정
   */
  protected void updateBy(final String username) {
    this.updatedBy = username;
  }

  /**
   * 엔티티를 삭제할 때 호출하여 deletedBy를 설정합니다.
   *
   * @param username 삭제자 계정
   */
  protected void deleteBy(final String username) {
    this.deletedBy = username;
  }

  /**
   * 논리 삭제를 수행합니다.
   *
   * <ul>
   *   <li>isDeleted를 true로 설정
   *   <li>deletedAt을 현재 시각으로 설정
   *   <li>deleteId를 현재 인증된 회원 이름 또는 "SYSTEM"로 설정
   * </ul>
   */
  public void delete(String deleteBy) {
    this.deletedAt = LocalDateTime.now();
    deleteBy(Objects.isNull(deleteBy) ? "SYSTEM" : deleteBy);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AbstractAuditEntity entity)) {
      return false;
    }

    return super.equals(o)
        && Objects.equals(createdBy, entity.createdBy)
        && Objects.equals(updatedBy, entity.updatedBy)
        && Objects.equals(deletedBy, entity.deletedBy)
        && Objects.equals(deletedAt, entity.deletedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), createdBy, updatedBy, deletedBy, deletedAt);
  }
}
