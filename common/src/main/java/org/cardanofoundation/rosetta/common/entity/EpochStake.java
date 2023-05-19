package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.ConstraintMode;
import java.math.BigInteger;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.cardanofoundation.rosetta.common.validation.Word31Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "epoch_stake", uniqueConstraints = {
    @UniqueConstraint(name = "unique_stake",
        columnNames = {"epoch_no", "addr_id", "pool_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class EpochStake extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "addr_id", nullable = false,
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
  @EqualsAndHashCode.Exclude
  private StakeAddress addr;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pool_id", nullable = false,
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
  @EqualsAndHashCode.Exclude
  private PoolHash pool;

  @Column(name = "amount", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger amount;

  @Column(name = "epoch_no", nullable = false)
  @Word31Type
  private Integer epochNo;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    EpochStake that = (EpochStake) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
