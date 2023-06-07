package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.cardanofoundation.rosetta.common.enumeration.BlocksDeletionStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
    name = "rollback_history"
)
public class RollbackHistory extends BaseEntity {
  @Column(
      name = "block_no_start",
      nullable = false
  )
  private Long blockNoStart;
  @Column(
      name = "block_slot_start",
      nullable = false
  )
  private Long blockSlotStart;
  @Column(
      name = "block_hash_start",
      nullable = false
  )
  private String blockHashStart;
  @Column(
      name = "block_no_end",
      nullable = false
  )
  private Long blockNoEnd;
  @Column(
      name = "block_slot_end",
      nullable = false
  )
  private Long blockSlotEnd;
  @Column(
      name = "block_hash_end",
      nullable = false
  )
  private String blockHashEnd;
  @Column(
      name = "reason"
  )
  private String reason;
  @Column(
      name = "rollback_time",
      nullable = false
  )
  private Timestamp rollbackTime;
  @Column(
      name = "blocks_deletion_status",
      nullable = false
  )
  private BlocksDeletionStatus blocksDeletionStatus;

  @PrePersist
  private void prePersist() {
    this.rollbackTime = Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC));
  }

  protected RollbackHistory(final RollbackHistory.RollbackHistoryBuilder<?, ?> b) {
    super(b);
    this.blockNoStart = b.blockNoStart;
    this.blockSlotStart = b.blockSlotStart;
    this.blockHashStart = b.blockHashStart;
    this.blockNoEnd = b.blockNoEnd;
    this.blockSlotEnd = b.blockSlotEnd;
    this.blockHashEnd = b.blockHashEnd;
    this.reason = b.reason;
    this.rollbackTime = b.rollbackTime;
    this.blocksDeletionStatus = b.blocksDeletionStatus;
  }

  public static RollbackHistory.RollbackHistoryBuilder<?, ?> builder() {
    return new RollbackHistory.RollbackHistoryBuilderImpl();
  }

  public RollbackHistory.RollbackHistoryBuilder<?, ?> toBuilder() {
    return (new RollbackHistory.RollbackHistoryBuilderImpl()).$fillValuesFrom(this);
  }

  public Long getBlockNoStart() {
    return this.blockNoStart;
  }

  public Long getBlockSlotStart() {
    return this.blockSlotStart;
  }

  public String getBlockHashStart() {
    return this.blockHashStart;
  }

  public Long getBlockNoEnd() {
    return this.blockNoEnd;
  }

  public Long getBlockSlotEnd() {
    return this.blockSlotEnd;
  }

  public String getBlockHashEnd() {
    return this.blockHashEnd;
  }

  public String getReason() {
    return this.reason;
  }

  public Timestamp getRollbackTime() {
    return this.rollbackTime;
  }

  public BlocksDeletionStatus getBlocksDeletionStatus() {
    return this.blocksDeletionStatus;
  }

  public void setBlockNoStart(final Long blockNoStart) {
    this.blockNoStart = blockNoStart;
  }

  public void setBlockSlotStart(final Long blockSlotStart) {
    this.blockSlotStart = blockSlotStart;
  }

  public void setBlockHashStart(final String blockHashStart) {
    this.blockHashStart = blockHashStart;
  }

  public void setBlockNoEnd(final Long blockNoEnd) {
    this.blockNoEnd = blockNoEnd;
  }

  public void setBlockSlotEnd(final Long blockSlotEnd) {
    this.blockSlotEnd = blockSlotEnd;
  }

  public void setBlockHashEnd(final String blockHashEnd) {
    this.blockHashEnd = blockHashEnd;
  }

  public void setReason(final String reason) {
    this.reason = reason;
  }

  public void setRollbackTime(final Timestamp rollbackTime) {
    this.rollbackTime = rollbackTime;
  }

  public void setBlocksDeletionStatus(final BlocksDeletionStatus blocksDeletionStatus) {
    this.blocksDeletionStatus = blocksDeletionStatus;
  }

  public RollbackHistory() {
  }

  public RollbackHistory(final Long blockNoStart, final Long blockSlotStart, final String blockHashStart, final Long blockNoEnd, final Long blockSlotEnd, final String blockHashEnd, final String reason, final Timestamp rollbackTime, final BlocksDeletionStatus blocksDeletionStatus) {
    this.blockNoStart = blockNoStart;
    this.blockSlotStart = blockSlotStart;
    this.blockHashStart = blockHashStart;
    this.blockNoEnd = blockNoEnd;
    this.blockSlotEnd = blockSlotEnd;
    this.blockHashEnd = blockHashEnd;
    this.reason = reason;
    this.rollbackTime = rollbackTime;
    this.blocksDeletionStatus = blocksDeletionStatus;
  }

  private static final class RollbackHistoryBuilderImpl extends RollbackHistory.RollbackHistoryBuilder<RollbackHistory, RollbackHistory.RollbackHistoryBuilderImpl> {
    private RollbackHistoryBuilderImpl() {
    }

    protected RollbackHistory.RollbackHistoryBuilderImpl self() {
      return this;
    }

    public RollbackHistory build() {
      return new RollbackHistory(this);
    }
  }

  public abstract static class RollbackHistoryBuilder<C extends RollbackHistory, B extends RollbackHistory.RollbackHistoryBuilder<C, B>> extends BaseEntityBuilder<C, B> {
    private Long blockNoStart;
    private Long blockSlotStart;
    private String blockHashStart;
    private Long blockNoEnd;
    private Long blockSlotEnd;
    private String blockHashEnd;
    private String reason;
    private Timestamp rollbackTime;
    private BlocksDeletionStatus blocksDeletionStatus;

    public RollbackHistoryBuilder() {
    }

    protected B $fillValuesFrom(final C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(final RollbackHistory instance, final RollbackHistory.RollbackHistoryBuilder<?, ?> b) {
      b.blockNoStart(instance.blockNoStart);
      b.blockSlotStart(instance.blockSlotStart);
      b.blockHashStart(instance.blockHashStart);
      b.blockNoEnd(instance.blockNoEnd);
      b.blockSlotEnd(instance.blockSlotEnd);
      b.blockHashEnd(instance.blockHashEnd);
      b.reason(instance.reason);
      b.rollbackTime(instance.rollbackTime);
      b.blocksDeletionStatus(instance.blocksDeletionStatus);
    }

    protected abstract B self();

    public abstract C build();

    public B blockNoStart(final Long blockNoStart) {
      this.blockNoStart = blockNoStart;
      return this.self();
    }

    public B blockSlotStart(final Long blockSlotStart) {
      this.blockSlotStart = blockSlotStart;
      return this.self();
    }

    public B blockHashStart(final String blockHashStart) {
      this.blockHashStart = blockHashStart;
      return this.self();
    }

    public B blockNoEnd(final Long blockNoEnd) {
      this.blockNoEnd = blockNoEnd;
      return this.self();
    }

    public B blockSlotEnd(final Long blockSlotEnd) {
      this.blockSlotEnd = blockSlotEnd;
      return this.self();
    }

    public B blockHashEnd(final String blockHashEnd) {
      this.blockHashEnd = blockHashEnd;
      return this.self();
    }

    public B reason(final String reason) {
      this.reason = reason;
      return this.self();
    }

    public B rollbackTime(final Timestamp rollbackTime) {
      this.rollbackTime = rollbackTime;
      return this.self();
    }

    public B blocksDeletionStatus(final BlocksDeletionStatus blocksDeletionStatus) {
      this.blocksDeletionStatus = blocksDeletionStatus;
      return this.self();
    }

    public String toString() {
      String var10000 = super.toString();
      return "RollbackHistory.RollbackHistoryBuilder(super=" + var10000 + ", blockNoStart=" + this.blockNoStart + ", blockSlotStart=" + this.blockSlotStart + ", blockHashStart=" + this.blockHashStart + ", blockNoEnd=" + this.blockNoEnd + ", blockSlotEnd=" + this.blockSlotEnd + ", blockHashEnd=" + this.blockHashEnd + ", reason=" + this.reason + ", rollbackTime=" + this.rollbackTime + ", blocksDeletionStatus=" + this.blocksDeletionStatus + ")";
    }
  }
}

