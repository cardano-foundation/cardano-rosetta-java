package org.cardanofoundation.rosetta.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pool_hash", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pool_hash",
        columnNames = {"hash_raw"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@DynamicUpdate
public class PoolHash extends BaseEntity {

  @Column(name = "hash_raw", nullable = false, length = 56)
  @Hash28Type
  private String hashRaw;

  @Column(name = "view", nullable = false)
  private String view;

  @Digits(integer = 20, fraction = 0)
  @Column(name = "pool_size", nullable = false, precision = 20)
  private BigInteger poolSize;

  @OneToMany(mappedBy = "poolHash")
  private List<Delegation> delegations;

  @Column(name = "epoch_no")
  private Integer epochNo;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    PoolHash poolHash = (PoolHash) o;
    return id != null && Objects.equals(id, poolHash.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
