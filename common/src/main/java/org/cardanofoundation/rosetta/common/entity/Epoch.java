package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import org.cardanofoundation.rosetta.common.enumeration.EraType;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.cardanofoundation.rosetta.common.validation.Word128Type;
import org.cardanofoundation.rosetta.common.validation.Word31Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(
        name = "epoch",
        uniqueConstraints = {@UniqueConstraint(
                name = "unique_epoch",
                columnNames = {"no"}
        )}
)
@DynamicUpdate
public class Epoch extends BaseEntity {
  @Column(
          name = "out_sum",
          nullable = false,
          precision = 39
  )
  @Word128Type
  private @Digits(
          integer = 39,
          fraction = 0
  ) BigInteger outSum;
  @Column(
          name = "fees",
          nullable = false,
          precision = 20
  )
  @Lovelace
  private @Digits(
          integer = 20,
          fraction = 0
  ) BigInteger fees;
  @Column(
          name = "tx_count",
          nullable = false
  )
  @Word31Type
  private Integer txCount;
  @Column(
          name = "blk_count",
          nullable = false
  )
  @Word31Type
  private Integer blkCount;
  @Column(
          name = "no",
          nullable = false
  )
  @Word31Type
  private Integer no;
  @Column(
          name = "start_time"
  )
  private Timestamp startTime;
  @Column(
          name = "end_time"
  )
  private Timestamp endTime;
  @Column(
          name = "max_slot",
          nullable = false
  )
  private Integer maxSlot;
  @Column(
          name = "era",
          nullable = false
  )
  private EraType era;
  @Column(
          name = "rewards_distributed"
  )
  @Lovelace
  private @Digits(
          integer = 20,
          fraction = 0
  ) BigInteger rewardsDistributed;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
      Epoch epoch = (Epoch)o;
      if (Objects.nonNull(this.id)) {
        if (!Objects.equals(this.id, epoch.id)) {
          return Boolean.FALSE;
        } else {
          return Objects.equals(this.outSum, epoch.getOutSum()) && Objects.equals(this.fees, epoch.getFees()) && Objects.equals(this.txCount, epoch.getTxCount()) && Objects.equals(this.blkCount, epoch.getBlkCount()) && Objects.equals(this.no, epoch.getNo()) && Objects.equals(this.startTime, epoch.getStartTime()) && Objects.equals(this.endTime, epoch.getEndTime()) && Objects.equals(this.maxSlot, epoch.getMaxSlot()) && Objects.equals(this.era, epoch.getEra()) && Objects.equals(this.rewardsDistributed, epoch.getRewardsDistributed());
        }
      } else {
        return Boolean.FALSE;
      }
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.getClass().hashCode();
  }

  protected Epoch(EpochBuilder<?, ?> b) {
    super(b);
    this.outSum = b.outSum;
    this.fees = b.fees;
    this.txCount = b.txCount;
    this.blkCount = b.blkCount;
    this.no = b.no;
    this.startTime = b.startTime;
    this.endTime = b.endTime;
    this.maxSlot = b.maxSlot;
    this.era = b.era;
    this.rewardsDistributed = b.rewardsDistributed;
  }

  public static EpochBuilder<?, ?> builder() {
    return new EpochBuilderImpl();
  }

  public EpochBuilder<?, ?> toBuilder() {
    return (new EpochBuilderImpl()).$fillValuesFrom(this);
  }

  public BigInteger getOutSum() {
    return this.outSum;
  }

  public BigInteger getFees() {
    return this.fees;
  }

  public Integer getTxCount() {
    return this.txCount;
  }

  public Integer getBlkCount() {
    return this.blkCount;
  }

  public Integer getNo() {
    return this.no;
  }

  public Timestamp getStartTime() {
    return this.startTime;
  }

  public Timestamp getEndTime() {
    return this.endTime;
  }

  public Integer getMaxSlot() {
    return this.maxSlot;
  }

  public EraType getEra() {
    return this.era;
  }

  public BigInteger getRewardsDistributed() {
    return this.rewardsDistributed;
  }

  public void setOutSum(BigInteger outSum) {
    this.outSum = outSum;
  }

  public void setFees(BigInteger fees) {
    this.fees = fees;
  }

  public void setTxCount(Integer txCount) {
    this.txCount = txCount;
  }

  public void setBlkCount(Integer blkCount) {
    this.blkCount = blkCount;
  }

  public void setNo(Integer no) {
    this.no = no;
  }

  public void setStartTime(Timestamp startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(Timestamp endTime) {
    this.endTime = endTime;
  }

  public void setMaxSlot(Integer maxSlot) {
    this.maxSlot = maxSlot;
  }

  public void setEra(EraType era) {
    this.era = era;
  }

  public void setRewardsDistributed(BigInteger rewardsDistributed) {
    this.rewardsDistributed = rewardsDistributed;
  }

  public Epoch() {
  }

  public Epoch(BigInteger outSum, BigInteger fees, Integer txCount, Integer blkCount, Integer no, Timestamp startTime, Timestamp endTime, Integer maxSlot, EraType era, BigInteger rewardsDistributed) {
    this.outSum = outSum;
    this.fees = fees;
    this.txCount = txCount;
    this.blkCount = blkCount;
    this.no = no;
    this.startTime = startTime;
    this.endTime = endTime;
    this.maxSlot = maxSlot;
    this.era = era;
    this.rewardsDistributed = rewardsDistributed;
  }

  public abstract static class EpochBuilder<C extends Epoch, B extends EpochBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
    private BigInteger outSum;
    private BigInteger fees;
    private Integer txCount;
    private Integer blkCount;
    private Integer no;
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer maxSlot;
    private EraType era;
    private BigInteger rewardsDistributed;

    public EpochBuilder() {
    }

    protected B $fillValuesFrom(C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(Epoch instance, EpochBuilder<?, ?> b) {
      b.outSum(instance.outSum);
      b.fees(instance.fees);
      b.txCount(instance.txCount);
      b.blkCount(instance.blkCount);
      b.no(instance.no);
      b.startTime(instance.startTime);
      b.endTime(instance.endTime);
      b.maxSlot(instance.maxSlot);
      b.era(instance.era);
      b.rewardsDistributed(instance.rewardsDistributed);
    }

    public B outSum(BigInteger outSum) {
      this.outSum = outSum;
      return this.self();
    }

    public B fees(BigInteger fees) {
      this.fees = fees;
      return this.self();
    }

    public B txCount(Integer txCount) {
      this.txCount = txCount;
      return this.self();
    }

    public B blkCount(Integer blkCount) {
      this.blkCount = blkCount;
      return this.self();
    }

    public B no(Integer no) {
      this.no = no;
      return this.self();
    }

    public B startTime(Timestamp startTime) {
      this.startTime = startTime;
      return this.self();
    }

    public B endTime(Timestamp endTime) {
      this.endTime = endTime;
      return this.self();
    }

    public B maxSlot(Integer maxSlot) {
      this.maxSlot = maxSlot;
      return this.self();
    }

    public B era(EraType era) {
      this.era = era;
      return this.self();
    }

    public B rewardsDistributed(BigInteger rewardsDistributed) {
      this.rewardsDistributed = rewardsDistributed;
      return this.self();
    }

    protected abstract B self();

    public abstract C build();

    public String toString() {
      String var10000 = super.toString();
      return "Epoch.EpochBuilder(super=" + var10000 + ", outSum=" + this.outSum + ", fees=" + this.fees + ", txCount=" + this.txCount + ", blkCount=" + this.blkCount + ", no=" + this.no + ", startTime=" + this.startTime + ", endTime=" + this.endTime + ", maxSlot=" + this.maxSlot + ", era=" + this.era + ", rewardsDistributed=" + this.rewardsDistributed + ")";
    }
  }

  private static final class EpochBuilderImpl extends EpochBuilder<Epoch, EpochBuilderImpl> {
    private EpochBuilderImpl() {
    }

    protected EpochBuilderImpl self() {
      return this;
    }

    public Epoch build() {
      return new Epoch(this);
    }
  }
}
