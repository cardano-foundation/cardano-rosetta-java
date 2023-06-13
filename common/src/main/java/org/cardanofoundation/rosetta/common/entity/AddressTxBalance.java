package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import org.cardanofoundation.rosetta.common.validation.Word128Type;
import org.hibernate.Hibernate;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(
        name = "address_tx_balance"
)
public class AddressTxBalance extends BaseEntity {
  @ManyToOne(
          fetch = FetchType.LAZY,
          optional = false
  )
  @JoinColumn(
          name = "address_id",
          nullable = false,
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private Address address;
  @Column(
          name = "address_id",
          updatable = false,
          insertable = false
  )
  private Long addressId;
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
  @ManyToOne(
          fetch = FetchType.LAZY
  )
  @JoinColumn(
          name = "stake_address_id",
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private StakeAddress stakeAddress;
  @Column(
          name = "balance",
          nullable = false,
          precision = 39
  )
  @Word128Type
  private @Digits(
          integer = 39,
          fraction = 0
  ) BigInteger balance;
  @Column(
          name = "time"
  )
  private Timestamp time;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
      AddressTxBalance that = (AddressTxBalance)o;
      return this.id != null && Objects.equals(this.id, that.id);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.getClass().hashCode();
  }

  protected AddressTxBalance(AddressTxBalanceBuilder<?, ?> b) {
    super(b);
    this.address = b.address;
    this.addressId = b.addressId;
    this.tx = b.tx;
    this.txId = b.txId;
    this.stakeAddress = b.stakeAddress;
    this.balance = b.balance;
    this.time = b.time;
  }

  public static AddressTxBalanceBuilder<?, ?> builder() {
    return new AddressTxBalanceBuilderImpl();
  }

  public AddressTxBalanceBuilder<?, ?> toBuilder() {
    return (new AddressTxBalanceBuilderImpl()).$fillValuesFrom(this);
  }

  public Address getAddress() {
    return this.address;
  }

  public Long getAddressId() {
    return this.addressId;
  }

  public Tx getTx() {
    return this.tx;
  }

  public Long getTxId() {
    return this.txId;
  }

  public StakeAddress getStakeAddress() {
    return this.stakeAddress;
  }

  public BigInteger getBalance() {
    return this.balance;
  }

  public Timestamp getTime() {
    return this.time;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public void setAddressId(Long addressId) {
    this.addressId = addressId;
  }

  public void setTx(Tx tx) {
    this.tx = tx;
  }

  public void setTxId(Long txId) {
    this.txId = txId;
  }

  public void setStakeAddress(StakeAddress stakeAddress) {
    this.stakeAddress = stakeAddress;
  }

  public void setBalance(BigInteger balance) {
    this.balance = balance;
  }

  public void setTime(Timestamp time) {
    this.time = time;
  }

  public AddressTxBalance() {
  }

  public AddressTxBalance(Address address, Long addressId, Tx tx, Long txId, StakeAddress stakeAddress, BigInteger balance, Timestamp time) {
    this.address = address;
    this.addressId = addressId;
    this.tx = tx;
    this.txId = txId;
    this.stakeAddress = stakeAddress;
    this.balance = balance;
    this.time = time;
  }

  public abstract static class AddressTxBalanceBuilder<C extends AddressTxBalance, B extends AddressTxBalanceBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
    private Address address;
    private Long addressId;
    private Tx tx;
    private Long txId;
    private StakeAddress stakeAddress;
    private BigInteger balance;
    private Timestamp time;

    public AddressTxBalanceBuilder() {
    }

    protected B $fillValuesFrom(C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(AddressTxBalance instance, AddressTxBalanceBuilder<?, ?> b) {
      b.address(instance.address);
      b.addressId(instance.addressId);
      b.tx(instance.tx);
      b.txId(instance.txId);
      b.stakeAddress(instance.stakeAddress);
      b.balance(instance.balance);
      b.time(instance.time);
    }

    public B address(Address address) {
      this.address = address;
      return this.self();
    }

    public B addressId(Long addressId) {
      this.addressId = addressId;
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

    public B stakeAddress(StakeAddress stakeAddress) {
      this.stakeAddress = stakeAddress;
      return this.self();
    }

    public B balance(BigInteger balance) {
      this.balance = balance;
      return this.self();
    }

    public B time(Timestamp time) {
      this.time = time;
      return this.self();
    }

    protected abstract B self();

    public abstract C build();

    public String toString() {
      String var10000 = super.toString();
      return "AddressTxBalance.AddressTxBalanceBuilder(super=" + var10000 + ", address=" + this.address + ", addressId=" + this.addressId + ", tx=" + this.tx + ", txId=" + this.txId + ", stakeAddress=" + this.stakeAddress + ", balance=" + this.balance + ", time=" + this.time + ")";
    }
  }

  private static final class AddressTxBalanceBuilderImpl extends AddressTxBalanceBuilder<AddressTxBalance, AddressTxBalanceBuilderImpl> {
    private AddressTxBalanceBuilderImpl() {
    }

    protected AddressTxBalanceBuilderImpl self() {
      return this;
    }

    public AddressTxBalance build() {
      return new AddressTxBalance(this);
    }
  }
}
