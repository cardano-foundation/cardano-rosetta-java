package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Objects;
import org.cardanofoundation.rosetta.common.enumeration.EraType;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.cardanofoundation.rosetta.common.validation.Word128Type;
import org.cardanofoundation.rosetta.common.validation.Word31Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

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
  @Digits(
      integer = 39,
      fraction = 0
  )
  private BigInteger outSum;
  @Column(
      name = "fees",
      nullable = false,
      precision = 20
  )
  @Lovelace
  @Digits(
      integer = 20,
      fraction = 0
  )
  private BigInteger fees;
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
  @Digits(
      integer = 20,
      fraction = 0
  )
  @Lovelace
  private BigInteger rewardsDistributed;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
      Epoch epoch = (Epoch)o;
      return this.id != null && Objects.equals(this.id, epoch.id);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.getClass().hashCode();
  }

  protected Epoch(final Epoch.EpochBuilder<?, ?> b) {
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

  public static Epoch.EpochBuilder<?, ?> builder() {
    return new Epoch.EpochBuilderImpl();
  }

  public Epoch.EpochBuilder<?, ?> toBuilder() {
    return (new Epoch.EpochBuilderImpl()).$fillValuesFrom(this);
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

  public void setOutSum(final BigInteger outSum) {
    this.outSum = outSum;
  }

  public void setFees(final BigInteger fees) {
    this.fees = fees;
  }

  public void setTxCount(final Integer txCount) {
    this.txCount = txCount;
  }

  public void setBlkCount(final Integer blkCount) {
    this.blkCount = blkCount;
  }

  public void setNo(final Integer no) {
    this.no = no;
  }

  public void setStartTime(final Timestamp startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(final Timestamp endTime) {
    this.endTime = endTime;
  }

  public void setMaxSlot(final Integer maxSlot) {
    this.maxSlot = maxSlot;
  }

  public void setEra(final EraType era) {
    this.era = era;
  }

  public void setRewardsDistributed(final BigInteger rewardsDistributed) {
    this.rewardsDistributed = rewardsDistributed;
  }

  public Epoch() {
  }

  public Epoch(final BigInteger outSum, final BigInteger fees, final Integer txCount, final Integer blkCount, final Integer no, final Timestamp startTime, final Timestamp endTime, final Integer maxSlot, final EraType era, final BigInteger rewardsDistributed) {
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

  private static final class EpochBuilderImpl extends Epoch.EpochBuilder<Epoch, Epoch.EpochBuilderImpl> {
    private EpochBuilderImpl() {
    }

    protected Epoch.EpochBuilderImpl self() {
      return this;
    }

    public Epoch build() {
      return new Epoch(this);
    }
  }

  public abstract static class EpochBuilder<C extends Epoch, B extends Epoch.EpochBuilder<C, B>> extends BaseEntityBuilder<C, B> {
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

    protected B $fillValuesFrom(final C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(final Epoch instance, final Epoch.EpochBuilder<?, ?> b) {
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

    protected abstract B self();

    public abstract C build();

    public B outSum(final BigInteger outSum) {
      this.outSum = outSum;
      return this.self();
    }

    public B fees(final BigInteger fees) {
      this.fees = fees;
      return this.self();
    }

    public B txCount(final Integer txCount) {
      this.txCount = txCount;
      return this.self();
    }

    public B blkCount(final Integer blkCount) {
      this.blkCount = blkCount;
      return this.self();
    }

    public B no(final Integer no) {
      this.no = no;
      return this.self();
    }

    public B startTime(final Timestamp startTime) {
      this.startTime = startTime;
      return this.self();
    }

    public B endTime(final Timestamp endTime) {
      this.endTime = endTime;
      return this.self();
    }

    public B maxSlot(final Integer maxSlot) {
      this.maxSlot = maxSlot;
      return this.self();
    }

    public B era(final EraType era) {
      this.era = era;
      return this.self();
    }

    public B rewardsDistributed(final BigInteger rewardsDistributed) {
      this.rewardsDistributed = rewardsDistributed;
      return this.self();
    }

    public String toString() {
      String var10000 = super.toString();
      return "Epoch.EpochBuilder(super=" + var10000 + ", outSum=" + this.outSum + ", fees=" + this.fees + ", txCount=" + this.txCount + ", blkCount=" + this.blkCount + ", no=" + this.no + ", startTime=" + this.startTime + ", endTime=" + this.endTime + ", maxSlot=" + this.maxSlot + ", era=" + this.era + ", rewardsDistributed=" + this.rewardsDistributed + ")";
    }
  }
}
