package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Word128Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(
        name = "address_token_balance"
)
public class AddressTokenBalance extends BaseEntity {
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
          name = "ident",
          nullable = false,
          foreignKey = @ForeignKey(
                  value = ConstraintMode.NO_CONSTRAINT,
                  name = "none"
          )
  )
  private MultiAsset multiAsset;
  @Column(
          name = "ident",
          updatable = false,
          insertable = false
  )
  private Long multiAssetId;
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

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
      AddressTokenBalance that = (AddressTokenBalance)o;
      return this.id != null && Objects.equals(this.id, that.id);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.getClass().hashCode();
  }

  protected AddressTokenBalance(AddressTokenBalanceBuilder<?, ?> b) {
    super(b);
    this.address = b.address;
    this.addressId = b.addressId;
    this.multiAsset = b.multiAsset;
    this.multiAssetId = b.multiAssetId;
    this.stakeAddress = b.stakeAddress;
    this.balance = b.balance;
  }

  public static AddressTokenBalanceBuilder<?, ?> builder() {
    return new AddressTokenBalanceBuilderImpl();
  }

  public AddressTokenBalanceBuilder<?, ?> toBuilder() {
    return (new AddressTokenBalanceBuilderImpl()).$fillValuesFrom(this);
  }

  public Address getAddress() {
    return this.address;
  }

  public Long getAddressId() {
    return this.addressId;
  }

  public MultiAsset getMultiAsset() {
    return this.multiAsset;
  }

  public Long getMultiAssetId() {
    return this.multiAssetId;
  }

  public StakeAddress getStakeAddress() {
    return this.stakeAddress;
  }

  public BigInteger getBalance() {
    return this.balance;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public void setAddressId(Long addressId) {
    this.addressId = addressId;
  }

  public void setMultiAsset(MultiAsset multiAsset) {
    this.multiAsset = multiAsset;
  }

  public void setMultiAssetId(Long multiAssetId) {
    this.multiAssetId = multiAssetId;
  }

  public void setStakeAddress(StakeAddress stakeAddress) {
    this.stakeAddress = stakeAddress;
  }

  public void setBalance(BigInteger balance) {
    this.balance = balance;
  }

  public AddressTokenBalance() {
  }

  public AddressTokenBalance(Address address, Long addressId, MultiAsset multiAsset, Long multiAssetId, StakeAddress stakeAddress, BigInteger balance) {
    this.address = address;
    this.addressId = addressId;
    this.multiAsset = multiAsset;
    this.multiAssetId = multiAssetId;
    this.stakeAddress = stakeAddress;
    this.balance = balance;
  }

  public abstract static class AddressTokenBalanceBuilder<C extends AddressTokenBalance, B extends AddressTokenBalanceBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
    private Address address;
    private Long addressId;
    private MultiAsset multiAsset;
    private Long multiAssetId;
    private StakeAddress stakeAddress;
    private BigInteger balance;

    public AddressTokenBalanceBuilder() {
    }

    protected B $fillValuesFrom(C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(AddressTokenBalance instance, AddressTokenBalanceBuilder<?, ?> b) {
      b.address(instance.address);
      b.addressId(instance.addressId);
      b.multiAsset(instance.multiAsset);
      b.multiAssetId(instance.multiAssetId);
      b.stakeAddress(instance.stakeAddress);
      b.balance(instance.balance);
    }

    public B address(Address address) {
      this.address = address;
      return this.self();
    }

    public B addressId(Long addressId) {
      this.addressId = addressId;
      return this.self();
    }

    public B multiAsset(MultiAsset multiAsset) {
      this.multiAsset = multiAsset;
      return this.self();
    }

    public B multiAssetId(Long multiAssetId) {
      this.multiAssetId = multiAssetId;
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

    protected abstract B self();

    public abstract C build();

    public String toString() {
      String var10000 = super.toString();
      return "AddressTokenBalance.AddressTokenBalanceBuilder(super=" + var10000 + ", address=" + this.address + ", addressId=" + this.addressId + ", multiAsset=" + this.multiAsset + ", multiAssetId=" + this.multiAssetId + ", stakeAddress=" + this.stakeAddress + ", balance=" + this.balance + ")";
    }
  }

  private static final class AddressTokenBalanceBuilderImpl extends AddressTokenBalanceBuilder<AddressTokenBalance, AddressTokenBalanceBuilderImpl> {
    private AddressTokenBalanceBuilderImpl() {
    }

    protected AddressTokenBalanceBuilderImpl self() {
      return this;
    }

    public AddressTokenBalance build() {
      return new AddressTokenBalance(this);
    }
  }
}
