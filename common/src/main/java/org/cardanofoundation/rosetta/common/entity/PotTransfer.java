package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Int65Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(name = "pot_transfer", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pot_transfer",
        columnNames = {"tx_id", "cert_index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PotTransfer extends BaseEntity {

  @Column(name = "cert_index", nullable = false)
  private Integer certIndex;

  @Column(name = "treasury", nullable = false, precision = 20)
  @Int65Type
  @Digits(integer = 20, fraction = 0)
  private BigInteger treasury;

  @Column(name = "reserves", nullable = false, precision = 20)
  @Int65Type
  @Digits(integer = 20, fraction = 0)
  private BigInteger reserves;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "tx_id", nullable = false,
      foreignKey = @ForeignKey(name = "pot_transfer_tx_id_fkey"))
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
    PotTransfer that = (PotTransfer) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
