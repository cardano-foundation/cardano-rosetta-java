package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.enumeration.converter.ByteConverter;
import org.cardanofoundation.rosetta.common.validation.Asset32Type;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.hibernate.Hibernate;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "multi_asset",
        uniqueConstraints = {@UniqueConstraint(
                name = "unique_multi_asset",
                columnNames = {"policy", "name"}
        )}
)
public class MultiAsset extends BaseEntity {
  @Column(
          name = "policy",
          nullable = false,
          length = 56
  )
  @Hash28Type
  private String policy;
  @Column(
          name = "name",
          nullable = false,
          length = 64
  )
  @Asset32Type
  @Convert(
          converter = ByteConverter.class
  )
  private String name;
  @Column(
          name = "name_view",
          length = 64
  )
  private String nameView;
  @Column(
          name = "fingerprint",
          nullable = false
  )
  private String fingerprint;
  @Column(
          name = "tx_count"
  )
  private Long txCount;
  @Column(
          name = "supply",
          precision = 23
  )
  private @Digits(
          integer = 23,
          fraction = 0
  ) BigInteger supply;
  @Column(
          name = "total_volume",
          precision = 40
  )
  private @Digits(
          integer = 40,
          fraction = 0
  ) BigInteger totalVolume;
  @Column(
          name = "time"
  )
  private Timestamp time;
  @OneToMany(
          fetch = FetchType.LAZY,
          mappedBy = "multiAsset"
  )
  private List<AddressToken> addressToken;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
      MultiAsset that = (MultiAsset)o;
      return this.id != null && Objects.equals(this.id, that.id);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.getClass().hashCode();
  }

  protected MultiAsset(MultiAssetBuilder<?, ?> b) {
    super(b);
    this.policy = b.policy;
    this.name = b.name;
    this.nameView = b.nameView;
    this.fingerprint = b.fingerprint;
    this.txCount = b.txCount;
    this.supply = b.supply;
    this.totalVolume = b.totalVolume;
    this.time = b.time;
    this.addressToken = b.addressToken;
  }

  public static MultiAssetBuilder<?, ?> builder() {
    return new MultiAssetBuilderImpl();
  }

  public MultiAssetBuilder<?, ?> toBuilder() {
    return (new MultiAssetBuilderImpl()).$fillValuesFrom(this);
  }

  public String getPolicy() {
    return this.policy;
  }

  public String getName() {
    return this.name;
  }

  public String getNameView() {
    return this.nameView;
  }

  public String getFingerprint() {
    return this.fingerprint;
  }

  public Long getTxCount() {
    return this.txCount;
  }

  public BigInteger getSupply() {
    return this.supply;
  }

  public BigInteger getTotalVolume() {
    return this.totalVolume;
  }

  public Timestamp getTime() {
    return this.time;
  }

  public List<AddressToken> getAddressToken() {
    return this.addressToken;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNameView(String nameView) {
    this.nameView = nameView;
  }

  public void setFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }

  public void setTxCount(Long txCount) {
    this.txCount = txCount;
  }

  public void setSupply(BigInteger supply) {
    this.supply = supply;
  }

  public void setTotalVolume(BigInteger totalVolume) {
    this.totalVolume = totalVolume;
  }

  public void setTime(Timestamp time) {
    this.time = time;
  }

  public void setAddressToken(List<AddressToken> addressToken) {
    this.addressToken = addressToken;
  }

  public MultiAsset() {
  }

  public MultiAsset(String policy, String name, String nameView, String fingerprint, Long txCount, BigInteger supply, BigInteger totalVolume, Timestamp time, List<AddressToken> addressToken) {
    this.policy = policy;
    this.name = name;
    this.nameView = nameView;
    this.fingerprint = fingerprint;
    this.txCount = txCount;
    this.supply = supply;
    this.totalVolume = totalVolume;
    this.time = time;
    this.addressToken = addressToken;
  }

  public abstract static class MultiAssetBuilder<C extends MultiAsset, B extends MultiAssetBuilder<C, B>> extends BaseEntity.BaseEntityBuilder<C, B> {
    private String policy;
    private String name;
    private String nameView;
    private String fingerprint;
    private Long txCount;
    private BigInteger supply;
    private BigInteger totalVolume;
    private Timestamp time;
    private List<AddressToken> addressToken;

    public MultiAssetBuilder() {
    }

    protected B $fillValuesFrom(C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(MultiAsset instance, MultiAssetBuilder<?, ?> b) {
      b.policy(instance.policy);
      b.name(instance.name);
      b.nameView(instance.nameView);
      b.fingerprint(instance.fingerprint);
      b.txCount(instance.txCount);
      b.supply(instance.supply);
      b.totalVolume(instance.totalVolume);
      b.time(instance.time);
      b.addressToken(instance.addressToken);
    }

    public B policy(String policy) {
      this.policy = policy;
      return this.self();
    }

    public B name(String name) {
      this.name = name;
      return this.self();
    }

    public B nameView(String nameView) {
      this.nameView = nameView;
      return this.self();
    }

    public B fingerprint(String fingerprint) {
      this.fingerprint = fingerprint;
      return this.self();
    }

    public B txCount(Long txCount) {
      this.txCount = txCount;
      return this.self();
    }

    public B supply(BigInteger supply) {
      this.supply = supply;
      return this.self();
    }

    public B totalVolume(BigInteger totalVolume) {
      this.totalVolume = totalVolume;
      return this.self();
    }

    public B time(Timestamp time) {
      this.time = time;
      return this.self();
    }

    public B addressToken(List<AddressToken> addressToken) {
      this.addressToken = addressToken;
      return this.self();
    }

    protected abstract B self();

    public abstract C build();

    public String toString() {
      String var10000 = super.toString();
      return "MultiAsset.MultiAssetBuilder(super=" + var10000 + ", policy=" + this.policy + ", name=" + this.name + ", nameView=" + this.nameView + ", fingerprint=" + this.fingerprint + ", txCount=" + this.txCount + ", supply=" + this.supply + ", totalVolume=" + this.totalVolume + ", time=" + this.time + ", addressToken=" + this.addressToken + ")";
    }
  }

  private static final class MultiAssetBuilderImpl extends MultiAssetBuilder<MultiAsset, MultiAssetBuilderImpl> {
    private MultiAssetBuilderImpl() {
    }

    protected MultiAssetBuilderImpl self() {
      return this;
    }

    public MultiAsset build() {
      return new MultiAsset(this);
    }
  }
}
