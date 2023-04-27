package org.cardanofoundation.rosetta.common.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Hash32Type;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(name = "pool_update", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pool_update",
        columnNames = {"registered_tx_id", "cert_index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PoolUpdate extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "hash_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_update_hash_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolHash poolHash;

  @Column(name = "hash_id", updatable = false, insertable = false)
  private Long poolHashId;

  @Column(name = "cert_index", nullable = false)
  private Integer certIndex;

  @Column(name = "vrf_key_hash", nullable = false, length = 64)
  @Hash32Type
  private String vrfKeyHash;

  @Column(name = "pledge", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger pledge;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "reward_addr_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_update_reward_addr_id_fkey"))
  @EqualsAndHashCode.Exclude
  private StakeAddress rewardAddr;

  @Column(name = "reward_addr_id", updatable = false, insertable = false)
  private Long rewardAddrId;

  @Column(name = "active_epoch_no", nullable = false)
  private Integer activeEpochNo;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "meta_id",
      foreignKey = @ForeignKey(name = "pool_update_meta_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolMetadataRef meta;

  @Column(name = "margin", nullable = false)
  private Double margin;

  @Column(name = "fixed_cost", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger fixedCost;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "registered_tx_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_update_registered_tx_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Tx registeredTx;

  @Column(name = "registered_tx_id", updatable = false, insertable = false)
  private Long registeredTxId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    PoolUpdate that = (PoolUpdate) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
