package org.cardanofoundation.rosetta.common.entity;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Addr29Type;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.cardanofoundation.rosetta.common.validation.Word128Type;
import org.hibernate.Hibernate;

@Entity
@Table(name = "stake_address", uniqueConstraints = {
    @UniqueConstraint(name = "unique_stake_address",
        columnNames = {"hash_raw"}
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class StakeAddress extends BaseEntity {

  @Column(name = "hash_raw", nullable = false)
  @Addr29Type
  private String hashRaw;

  @Column(name = "view", nullable = false, length = 65535)
  private String view;

  @Column(name = "script_hash", length = 56)
  @Hash28Type
  private String scriptHash;
  @Column(name = "created_at")
  private Timestamp createdAt;
  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @Column(name = "updated_at")
  private Timestamp updatedAt;

  @Column(name = "balance", nullable = false, precision = 39)
  @Word128Type
  @Digits(integer = 39, fraction = 0)
  private BigInteger balance;

  @Column(name = "available_reward", nullable = false, precision = 39)
  @Word128Type
  @Digits(integer = 39, fraction = 0)
  private BigInteger availableReward;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    StakeAddress that = (StakeAddress) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
