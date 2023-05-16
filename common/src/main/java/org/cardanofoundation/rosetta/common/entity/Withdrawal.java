package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(name = "withdrawal", uniqueConstraints = {
    @UniqueConstraint(name = "unique_slot_leader",
        columnNames = {"addr_id", "tx_id"}
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Withdrawal extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "addr_id", nullable = false,
      foreignKey = @ForeignKey(name = "withdrawal_addr_id_fkey"))
  @EqualsAndHashCode.Exclude
  private StakeAddress addr;

  @Column(name = "addr_id", updatable = false, insertable = false)
  private Long stakeAddressId;

  @Column(name = "amount", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger amount;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "redeemer_id",
      foreignKey = @ForeignKey(name = "withdrawal_redeemer_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Redeemer redeemer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "tx_id", nullable = false,
      foreignKey = @ForeignKey(name = "withdrawal_tx_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Tx tx;
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Withdrawal that = (Withdrawal) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
