package org.cardanofoundation.rosetta.common.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.enumeration.RewardType;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(name = "reward", uniqueConstraints = {
    @UniqueConstraint(name = "unique_reward",
        columnNames = {"addr_id", "type", "earned_epoch", "pool_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Reward extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "addr_id", nullable = false,
      foreignKey = @ForeignKey(name = "reward_addr_id_fkey"))
  @EqualsAndHashCode.Exclude
  private StakeAddress addr;

  @Column(name = "addr_id", updatable = false, insertable = false)
  private Long stakeAddressId;

  @Column(name = "type", nullable = false)
  private RewardType type;

  @Column(name = "amount", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger amount;

  @Column(name = "earned_epoch", nullable = false)
  private Integer earnedEpoch;

  @Column(name = "spendable_epoch", nullable = false)
  private Integer spendableEpoch;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "pool_id",
      foreignKey = @ForeignKey(name = "reward_pool_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolHash pool;

  @Column(name = "pool_id", updatable = false, insertable = false)
  private Long poolId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Reward reward = (Reward) o;
    return id != null && Objects.equals(id, reward.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
