package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Word63Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
@Table(name = "delegation", uniqueConstraints = {
    @UniqueConstraint(name = "unique_delegation",
        columnNames = {"tx_id", "cert_index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Delegation extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "addr_id", nullable = false,
      foreignKey = @ForeignKey(name = "delegation_addr_id_fkey"))
  @EqualsAndHashCode.Exclude
  private StakeAddress address;

  @Column(name = "cert_index", nullable = false)
  private Integer certIndex;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "pool_hash_id", nullable = false,
      foreignKey = @ForeignKey(name = "delegation_pool_hash_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolHash poolHash;

  @Column(name = "active_epoch_no", nullable = false)
  private Long activeEpochNo;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "tx_id", nullable = false,
      foreignKey = @ForeignKey(name = "delegation_tx_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Tx tx;

  @Column(name = "slot_no", nullable = false)
  @Word63Type
  private Long slotNo;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "redeemer_id",
      foreignKey = @ForeignKey(name = "delegation_redeemer_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Redeemer redeemer;

  @Column(name = "addr_id", updatable = false, insertable = false)
  private Long stakeAddressId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Delegation that = (Delegation) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
