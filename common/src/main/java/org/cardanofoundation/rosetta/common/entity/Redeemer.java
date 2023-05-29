package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.enumeration.ScriptPurposeType;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.cardanofoundation.rosetta.common.validation.Word31Type;
import org.cardanofoundation.rosetta.common.validation.Word63Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(name = "redeemer", uniqueConstraints = {
    @UniqueConstraint(name = "unique_redeemer",
        columnNames = {"tx_id", "purpose", "index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Redeemer extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "tx_id", nullable = false,
      foreignKey = @ForeignKey(name = "redeemer_tx_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Tx tx;

  @Column(name = "unit_mem", nullable = false)
  @Word63Type
  private Long unitMem;

  @Column(name = "unit_steps", nullable = false)
  @Word63Type
  private Long unitSteps;

  @Column(name = "fee", precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger fee;

  @Column(name = "purpose", nullable = false)
  private ScriptPurposeType purpose;

  @Column(name = "index", nullable = false)
  @Word31Type
  private Integer index;

  @Column(name = "script_hash", length = 56)
  @Hash28Type
  private String scriptHash;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "redeemer_data_id", nullable = false,
      foreignKey = @ForeignKey(name = "redeemer_redeemer_data_id_fkey"))
  @EqualsAndHashCode.Exclude
  private RedeemerData redeemerData;
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Redeemer redeemer = (Redeemer) o;
    return id != null && Objects.equals(id, redeemer.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
