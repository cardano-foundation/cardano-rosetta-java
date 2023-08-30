package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Word128Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(
        name = "address"
)
public class Address extends BaseEntity {
  @Column(
          name = "address",
          nullable = false,
          length = 65535
  )
  private String address;
  @Column(
          name = "tx_count"
  )
  private Long txCount;
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
          name = "address_has_script",
          nullable = false
  )
  private Boolean addressHasScript;
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
          name = "stake_address_id",
          updatable = false,
          insertable = false
  )
  private Long stakeAddressId;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
      Address address = (Address)o;
      return this.id != null && Objects.equals(this.id, address.id);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.getClass().hashCode();
  }

  protected Address(AddressBuilder<?, ?> b) {
    super(b);
    this.address = b.address;
    this.txCount = b.txCount;
    this.balance = b.balance;
    this.addressHasScript = b.addressHasScript;
    this.stakeAddress = b.stakeAddress;
    this.stakeAddressId = b.stakeAddressId;
  }

  public static AddressBuilder<?, ?> builder() {
    return new AddressBuilderImpl();
  }

  public AddressBuilder<?, ?> toBuilder() {
    return (new AddressBuilderImpl()).$fillValuesFrom(this);
  }

  public String getAddress() {
    return this.address;
  }

  public Long getTxCount() {
    return this.txCount;
  }

  public BigInteger getBalance() {
    return this.balance;
  }

  public Boolean getAddressHasScript() {
    return this.addressHasScript;
  }

  public StakeAddress getStakeAddress() {
    return this.stakeAddress;
  }

  public Long getStakeAddressId() {
    return this.stakeAddressId;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setTxCount(Long txCount) {
    this.txCount = txCount;
  }

  public void setBalance(BigInteger balance) {
    this.balance = balance;
  }

  public void setAddressHasScript(Boolean addressHasScript) {
    this.addressHasScript = addressHasScript;
  }

  public void setStakeAddress(StakeAddress stakeAddress) {
    this.stakeAddress = stakeAddress;
  }

  public void setStakeAddressId(Long stakeAddressId) {
    this.stakeAddressId = stakeAddressId;
  }

  public Address() {
  }

  public Address(String address, Long txCount, BigInteger balance, Boolean addressHasScript, StakeAddress stakeAddress, Long stakeAddressId) {
    this.address = address;
    this.txCount = txCount;
    this.balance = balance;
    this.addressHasScript = addressHasScript;
    this.stakeAddress = stakeAddress;
    this.stakeAddressId = stakeAddressId;
  }

  public abstract static class AddressBuilder<C extends Address, B extends AddressBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
    private String address;
    private Long txCount;
    private BigInteger balance;
    private Boolean addressHasScript;
    private StakeAddress stakeAddress;
    private Long stakeAddressId;

    public AddressBuilder() {
    }

    protected B $fillValuesFrom(C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(Address instance, AddressBuilder<?, ?> b) {
      b.address(instance.address);
      b.txCount(instance.txCount);
      b.balance(instance.balance);
      b.addressHasScript(instance.addressHasScript);
      b.stakeAddress(instance.stakeAddress);
      b.stakeAddressId(instance.stakeAddressId);
    }

    public B address(String address) {
      this.address = address;
      return this.self();
    }

    public B txCount(Long txCount) {
      this.txCount = txCount;
      return this.self();
    }

    public B balance(BigInteger balance) {
      this.balance = balance;
      return this.self();
    }

    public B addressHasScript(Boolean addressHasScript) {
      this.addressHasScript = addressHasScript;
      return this.self();
    }

    public B stakeAddress(StakeAddress stakeAddress) {
      this.stakeAddress = stakeAddress;
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
      return "Address.AddressBuilder(super=" + var10000 + ", address=" + this.address + ", txCount=" + this.txCount + ", balance=" + this.balance + ", addressHasScript=" + this.addressHasScript + ", stakeAddress=" + this.stakeAddress + ", stakeAddressId=" + this.stakeAddressId + ")";
    }
  }

  private static final class AddressBuilderImpl extends AddressBuilder<Address, AddressBuilderImpl> {
    private AddressBuilderImpl() {
    }

    protected AddressBuilderImpl self() {
      return this;
    }

    public Address build() {
      return new Address(this);
    }
  }
}
