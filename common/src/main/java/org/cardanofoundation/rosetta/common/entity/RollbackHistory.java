package org.cardanofoundation.rosetta.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
          name = "block_no",
          nullable = false
  )
  private Long blockNo;
  @Column(
          name = "block_hash",
          nullable = false,
          length = 64
  )
  private String blockHash;
  @Column(
          name = "slot_no",
          nullable = false
  )
  private Long slotNo;
  @JsonFormat(
          shape = JsonFormat.Shape.STRING,
          pattern = "yyyy-MM-dd HH:mm:ss"
  )
  @Column(
          name = "rollback_time",
          nullable = false
  )
  private Timestamp rollbackTime;

  @PrePersist
  private void prePersist() {
    this.rollbackTime = Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC));
  }

  protected RollbackHistory(RollbackHistoryBuilder<?, ?> b) {
    super(b);
    this.blockNo = b.blockNo;
    this.blockHash = b.blockHash;
    this.slotNo = b.slotNo;
    this.rollbackTime = b.rollbackTime;
  }

  public static RollbackHistoryBuilder<?, ?> builder() {
    return new RollbackHistoryBuilderImpl();
  }

  public RollbackHistoryBuilder<?, ?> toBuilder() {
    return (new RollbackHistoryBuilderImpl()).$fillValuesFrom(this);
  }

  public Long getBlockNo() {
    return this.blockNo;
  }

  public String getBlockHash() {
    return this.blockHash;
  }

  public Long getSlotNo() {
    return this.slotNo;
  }

  public Timestamp getRollbackTime() {
    return this.rollbackTime;
  }

  public void setBlockNo(Long blockNo) {
    this.blockNo = blockNo;
  }

  public void setBlockHash(String blockHash) {
    this.blockHash = blockHash;
  }

  public void setSlotNo(Long slotNo) {
    this.slotNo = slotNo;
  }

  @JsonFormat(
          shape = JsonFormat.Shape.STRING,
          pattern = "yyyy-MM-dd HH:mm:ss"
  )
  public void setRollbackTime(Timestamp rollbackTime) {
    this.rollbackTime = rollbackTime;
  }

  public RollbackHistory() {
  }

  public RollbackHistory(Long blockNo, String blockHash, Long slotNo, Timestamp rollbackTime) {
    this.blockNo = blockNo;
    this.blockHash = blockHash;
    this.slotNo = slotNo;
    this.rollbackTime = rollbackTime;
  }

  public abstract static class RollbackHistoryBuilder<C extends RollbackHistory, B extends RollbackHistoryBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
    private Long blockNo;
    private String blockHash;
    private Long slotNo;
    private Timestamp rollbackTime;

    public RollbackHistoryBuilder() {
    }

    protected B $fillValuesFrom(C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(RollbackHistory instance, RollbackHistoryBuilder<?, ?> b) {
      b.blockNo(instance.blockNo);
      b.blockHash(instance.blockHash);
      b.slotNo(instance.slotNo);
      b.rollbackTime(instance.rollbackTime);
    }

    public B blockNo(Long blockNo) {
      this.blockNo = blockNo;
      return this.self();
    }

    public B blockHash(String blockHash) {
      this.blockHash = blockHash;
      return this.self();
    }

    public B slotNo(Long slotNo) {
      this.slotNo = slotNo;
      return this.self();
    }

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    public B rollbackTime(Timestamp rollbackTime) {
      this.rollbackTime = rollbackTime;
      return this.self();
    }

    protected abstract B self();

    public abstract C build();

    public String toString() {
      String var10000 = super.toString();
      return "RollbackHistory.RollbackHistoryBuilder(super=" + var10000 + ", blockNo=" + this.blockNo + ", blockHash=" + this.blockHash + ", slotNo=" + this.slotNo + ", rollbackTime=" + this.rollbackTime + ")";
    }
  }

  private static final class RollbackHistoryBuilderImpl extends RollbackHistoryBuilder<RollbackHistory, RollbackHistoryBuilderImpl> {
    private RollbackHistoryBuilderImpl() {
    }

    protected RollbackHistoryBuilderImpl self() {
      return this;
    }

    public RollbackHistory build() {
      return new RollbackHistory(this);
    }
  }
}
