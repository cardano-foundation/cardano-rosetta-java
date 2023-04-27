package org.cardanofoundation.rosetta.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.enumeration.BlocksDeletionStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "rollback_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RollbackHistory extends BaseEntity {

  @Column(name = "block_no_start", nullable = false)
  private Long blockNoStart;

  @Column(name = "block_slot_start", nullable = false)
  private Long blockSlotStart;

  @Column(name = "block_hash_start", nullable = false)
  private String blockHashStart;

  @Column(name = "block_no_end", nullable = false)
  private Long blockNoEnd;

  @Column(name = "block_slot_end", nullable = false)
  private Long blockSlotEnd;

  @Column(name = "block_hash_end", nullable = false)
  private String blockHashEnd;

  @Column(name = "reason")
  private String reason;

  @Column(name = "rollback_time", nullable = false)
  private Timestamp rollbackTime;

  @Column(name = "blocks_deletion_status", nullable = false)
  private BlocksDeletionStatus blocksDeletionStatus;

  @PrePersist
  private void prePersist() {
    rollbackTime = Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC));
  }
}