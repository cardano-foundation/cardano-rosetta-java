package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
    name = "pool_hash",
    uniqueConstraints = {@UniqueConstraint(
        name = "unique_pool_hash",
        columnNames = {"hash_raw"}
    )}
)
@DynamicUpdate
public class PoolHash extends BaseEntity {
  @Column(
      name = "hash_raw",
      nullable = false,
      length = 56
  )
  @Hash28Type
  private String hashRaw;
  @Column(
      name = "view",
      nullable = false
  )
  private String view;
  @Digits(
      integer = 20,
      fraction = 0
  )
  @Column(
      name = "pool_size",
      nullable = false,
      precision = 20
  )
  private BigInteger poolSize;
  @OneToMany(
      mappedBy = "poolHash"
  )
  private List<Delegation> delegations;
  @Column(
      name = "epoch_no"
  )
  private Integer epochNo;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && Hibernate.getClass(this) == Hibernate.getClass(o)) {
      PoolHash poolHash = (PoolHash)o;
      return this.id != null && Objects.equals(this.id, poolHash.id);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return this.getClass().hashCode();
  }

  protected PoolHash(final PoolHash.PoolHashBuilder<?, ?> b) {
    super(b);
    this.hashRaw = b.hashRaw;
    this.view = b.view;
    this.poolSize = b.poolSize;
    this.delegations = b.delegations;
    this.epochNo = b.epochNo;
  }

  public static PoolHash.PoolHashBuilder<?, ?> builder() {
    return new PoolHash.PoolHashBuilderImpl();
  }

  public PoolHash.PoolHashBuilder<?, ?> toBuilder() {
    return (new PoolHash.PoolHashBuilderImpl()).$fillValuesFrom(this);
  }

  public String getHashRaw() {
    return this.hashRaw;
  }

  public String getView() {
    return this.view;
  }

  public BigInteger getPoolSize() {
    return this.poolSize;
  }

  public List<Delegation> getDelegations() {
    return this.delegations;
  }

  public Integer getEpochNo() {
    return this.epochNo;
  }

  public void setHashRaw(final String hashRaw) {
    this.hashRaw = hashRaw;
  }

  public void setView(final String view) {
    this.view = view;
  }

  public void setPoolSize(final BigInteger poolSize) {
    this.poolSize = poolSize;
  }

  public void setDelegations(final List<Delegation> delegations) {
    this.delegations = delegations;
  }

  public void setEpochNo(final Integer epochNo) {
    this.epochNo = epochNo;
  }

  public PoolHash() {
  }

  public PoolHash(final String hashRaw, final String view, final BigInteger poolSize, final List<Delegation> delegations, final Integer epochNo) {
    this.hashRaw = hashRaw;
    this.view = view;
    this.poolSize = poolSize;
    this.delegations = delegations;
    this.epochNo = epochNo;
  }

  private static final class PoolHashBuilderImpl extends PoolHash.PoolHashBuilder<PoolHash, PoolHash.PoolHashBuilderImpl> {
    private PoolHashBuilderImpl() {
    }

    protected PoolHash.PoolHashBuilderImpl self() {
      return this;
    }

    public PoolHash build() {
      return new PoolHash(this);
    }
  }

  public abstract static class PoolHashBuilder<C extends PoolHash, B extends PoolHash.PoolHashBuilder<C, B>> extends BaseEntityBuilder<C, B> {
    private String hashRaw;
    private String view;
    private BigInteger poolSize;
    private List<Delegation> delegations;
    private Integer epochNo;

    public PoolHashBuilder() {
    }

    protected B $fillValuesFrom(final C instance) {
      super.$fillValuesFrom(instance);
      $fillValuesFromInstanceIntoBuilder(instance, this);
      return this.self();
    }

    private static void $fillValuesFromInstanceIntoBuilder(final PoolHash instance, final PoolHash.PoolHashBuilder<?, ?> b) {
      b.hashRaw(instance.hashRaw);
      b.view(instance.view);
      b.poolSize(instance.poolSize);
      b.delegations(instance.delegations);
      b.epochNo(instance.epochNo);
    }

    protected abstract B self();

    public abstract C build();

    public B hashRaw(final String hashRaw) {
      this.hashRaw = hashRaw;
      return this.self();
    }

    public B view(final String view) {
      this.view = view;
      return this.self();
    }

    public B poolSize(final BigInteger poolSize) {
      this.poolSize = poolSize;
      return this.self();
    }

    public B delegations(final List<Delegation> delegations) {
      this.delegations = delegations;
      return this.self();
    }

    public B epochNo(final Integer epochNo) {
      this.epochNo = epochNo;
      return this.self();
    }

    public String toString() {
      String var10000 = super.toString();
      return "PoolHash.PoolHashBuilder(super=" + var10000 + ", hashRaw=" + this.hashRaw + ", view=" + this.view + ", poolSize=" + this.poolSize + ", delegations=" + this.delegations + ", epochNo=" + this.epochNo + ")";
    }
  }
}
