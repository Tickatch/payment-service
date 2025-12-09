package com.tickatch.paymentservice.global.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractAuditEntityTest {
  private static final String USERNAME = "username";

  private AbstractAuditEntity entity;

  @BeforeEach
  void setUp() {
    entity = new AbstractAuditEntity() {};
  }

  @Test
  void testCreateBy() {
    entity.createBy(USERNAME);

    assertThat(entity.getCreatedBy()).isEqualTo(USERNAME);
  }

  @Test
  void testUpdateBy() {
    entity.updateBy(USERNAME);

    assertThat(entity.getUpdatedBy()).isEqualTo(USERNAME);
  }

  @Test
  void testDeleteBy() {
    entity.deleteBy(USERNAME);

    assertThat(entity.getDeletedBy()).isEqualTo(USERNAME);
  }

  @Test
  void testDelete() {
    entity.delete(USERNAME);

    assertThat(entity.getDeletedAt()).isNotNull();
    assertThat(entity.getDeletedAt()).isBeforeOrEqualTo(LocalDateTime.now());

    assertThat(entity.getDeletedBy()).isEqualTo(USERNAME);
  }

  @Test
  void testDeleteWithoutAuthentication() {
    entity.delete(null);

    assertThat(entity.getDeletedBy()).isEqualTo("SYSTEM");
  }

  @Test
  void testEqual() {
    entity.createBy(USERNAME);
    entity.updateBy(USERNAME);

    AbstractAuditEntity newEntity = new AbstractAuditEntity() {};
    assertThat(newEntity.equals(entity)).isFalse();

    newEntity.createBy(USERNAME);
    newEntity.updateBy(USERNAME);
    assertThat(newEntity.equals(entity)).isTrue();
  }
}
