package org.cardanofoundation.rosetta.common.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.cardanofoundation.rosetta.common.validation.Word31Type;
import org.cardanofoundation.rosetta.common.validation.Word63Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(name = "ada_pots", uniqueConstraints = {
    @UniqueConstraint(name = "unique_ada_pots", columnNames = {"block_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AdaPots extends BaseEntity {

  @Column(name = "slot_no", nullable = false)
  @Word63Type
  private Long slotNo;

  @Column(name = "epoch_no", nullable = false)
  @Word31Type
  private Integer epochNo;

  @Column(name = "treasury", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger treasury;

  @Column(name = "reserves", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger reserves;

  @Column(name = "rewards", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger rewards;

  @Column(name = "utxo", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger utxo;

  @Column(name = "deposits", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger deposits;

  @Column(name = "fees", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger fees;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "block_id", nullable = false, unique = true,
      foreignKey = @ForeignKey(name = "ada_pots_block_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Block block;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    AdaPots adaPots = (AdaPots) o;
    return id != null && Objects.equals(id, adaPots.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
