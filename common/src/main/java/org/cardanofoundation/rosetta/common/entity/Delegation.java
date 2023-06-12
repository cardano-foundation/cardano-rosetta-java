package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Word63Type;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Table(
        name = "delegation",
        uniqueConstraints = {@UniqueConstraint(
                name = "unique_delegation",
                columnNames = {"tx_id", "cert_index"}
        )}
)
public class Delegation extends BaseEntity {
  @ManyToOne(
          fetch = FetchType.LAZY,
          optional = false
  )
  @JoinColumn(
          name = "addr_id",
          nullable = false,
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private StakeAddress address;
  @Column(
          name = "cert_index",
          nullable = false
  )
  private Integer certIndex;
  @ManyToOne(
          fetch = FetchType.LAZY,
          optional = false
  )
  @JoinColumn(
          name = "pool_hash_id",
          nullable = false,
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private PoolHash poolHash;
  @Column(
          name = "active_epoch_no",
          nullable = false
  )
  private Integer activeEpochNo;
  @ManyToOne(
          fetch = FetchType.LAZY,
          optional = false
  )
  @JoinColumn(
          name = "tx_id",
          nullable = false,
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private Tx tx;
  @Column(
          name = "tx_id",
          updatable = false,
          insertable = false
  )
  private Long txId;
  @Column(
          name = "slot_no",
          nullable = false
  )
  @Word63Type
  private Long slotNo;
  @ManyToOne(
          fetch = FetchType.LAZY
  )
  @JoinColumn(
          name = "redeemer_id",
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private Redeemer redeemer;
  @Column(
          name = "addr_id",
          updatable = false,
          insertable = false
  )
  private Long stakeAddressId;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
      Delegation that = (Delegation)o;
      return this.id != null && Objects.equals(this.id, that.id);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.getClass().hashCode();
  }

  protected Delegation(DelegationBuilder<?, ?> b) {
    super(b);
    this.address = b.address;
    this.certIndex = b.certIndex;
    this.poolHash = b.poolHash;
    this.activeEpochNo = b.activeEpochNo;
    this.tx = b.tx;
    this.txId = b.txId;
    this.slotNo = b.slotNo;
    this.redeemer = b.redeemer;
    this.stakeAddressId = b.stakeAddressId;
  }

  public static DelegationBuilder<?, ?> builder() {
    return new DelegationBuilderImpl();
  }

  public DelegationBuilder<?, ?> toBuilder() {
    return (new DelegationBuilderImpl()).$fillValuesFrom(this);
  }

  public StakeAddress getAddress() {
    return this.address;
  }

  public Integer getCertIndex() {
    return this.certIndex;
  }

  public PoolHash getPoolHash() {
    return this.poolHash;
  }

  public Integer getActiveEpochNo() {
    return this.activeEpochNo;
  }

  public Tx getTx() {
    return this.tx;
  }

  public Long getTxId() {
    return this.txId;
  }

  public Long getSlotNo() {
    return this.slotNo;
  }

  public Redeemer getRedeemer() {
    return this.redeemer;
  }

  public Long getStakeAddressId() {
    return this.stakeAddressId;
  }

  public void setAddress(StakeAddress address) {
    this.address = address;
  }

  public void setCertIndex(Integer certIndex) {
    this.certIndex = certIndex;
  }

  public void setPoolHash(PoolHash poolHash) {
    this.poolHash = poolHash;
  }

  public void setActiveEpochNo(Integer activeEpochNo) {
    this.activeEpochNo = activeEpochNo;
  }

  public void setTx(Tx tx) {
    this.tx = tx;
  }

  public void setTxId(Long txId) {
    this.txId = txId;
  }

  public void setSlotNo(Long slotNo) {
    this.slotNo = slotNo;
  }

  public void setRedeemer(Redeemer redeemer) {
    this.redeemer = redeemer;
  }

  public void setStakeAddressId(Long stakeAddressId) {
    this.stakeAddressId = stakeAddressId;
  }

  public Delegation() {
  }

  public Delegation(StakeAddress address, Integer certIndex, PoolHash poolHash, Integer activeEpochNo, Tx tx, Long txId, Long slotNo, Redeemer redeemer, Long stakeAddressId) {
    this.address = address;
    this.certIndex = certIndex;
    this.poolHash = poolHash;
    this.activeEpochNo = activeEpochNo;
    this.tx = tx;
    this.txId = txId;
    this.slotNo = slotNo;
    this.redeemer = redeemer;
    this.stakeAddressId = stakeAddressId;
  }

  public abstract static class DelegationBuilder<C extends Delegation, B extends DelegationBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
    private StakeAddress address;
    private Integer certIndex;
    private PoolHash poolHash;
    private Integer activeEpochNo;
    private Tx tx;
    private Long txId;
    private Long slotNo;
    private Redeemer redeemer;
    private Long stakeAddressId;

    public DelegationBuilder() {
    }

    protected B $fillValuesFrom(C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(Delegation instance, DelegationBuilder<?, ?> b) {
      b.address(instance.address);
      b.certIndex(instance.certIndex);
      b.poolHash(instance.poolHash);
      b.activeEpochNo(instance.activeEpochNo);
      b.tx(instance.tx);
      b.txId(instance.txId);
      b.slotNo(instance.slotNo);
      b.redeemer(instance.redeemer);
      b.stakeAddressId(instance.stakeAddressId);
    }

    public B address(StakeAddress address) {
      this.address = address;
      return this.self();
    }

    public B certIndex(Integer certIndex) {
      this.certIndex = certIndex;
      return this.self();
    }

    public B poolHash(PoolHash poolHash) {
      this.poolHash = poolHash;
      return this.self();
    }

    public B activeEpochNo(Integer activeEpochNo) {
      this.activeEpochNo = activeEpochNo;
      return this.self();
    }

    public B tx(Tx tx) {
      this.tx = tx;
      return this.self();
    }

    public B txId(Long txId) {
      this.txId = txId;
      return this.self();
    }

    public B slotNo(Long slotNo) {
      this.slotNo = slotNo;
      return this.self();
    }

    public B redeemer(Redeemer redeemer) {
      this.redeemer = redeemer;
      return this.self();
    }

    public B stakeAddressId(Long stakeAddressId) {
      this.stakeAddressId = stakeAddressId;
      return this.self();
    }

    protected abstract B self();

    public abstract C build();

    public String toString() {
      String var10000 = super.toString();
      return "Delegation.DelegationBuilder(super=" + var10000 + ", address=" + this.address + ", certIndex=" + this.certIndex + ", poolHash=" + this.poolHash + ", activeEpochNo=" + this.activeEpochNo + ", tx=" + this.tx + ", txId=" + this.txId + ", slotNo=" + this.slotNo + ", redeemer=" + this.redeemer + ", stakeAddressId=" + this.stakeAddressId + ")";
    }
  }

  private static final class DelegationBuilderImpl extends DelegationBuilder<Delegation, DelegationBuilderImpl> {
    private DelegationBuilderImpl() {
    }

    protected DelegationBuilderImpl self() {
      return this;
    }

    public Delegation build() {
      return new Delegation(this);
    }
  }
}
