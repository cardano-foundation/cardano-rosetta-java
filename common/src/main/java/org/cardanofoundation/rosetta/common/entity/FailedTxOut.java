package org.cardanofoundation.rosetta.common.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.cardanofoundation.rosetta.common.validation.Hash32Type;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.cardanofoundation.rosetta.common.validation.TxIndex;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(name = "failed_tx_out", uniqueConstraints = {
    @UniqueConstraint(name = "unique_col_failed_txout",
        columnNames = {"tx_id", "index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class FailedTxOut extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "tx_id", nullable = false,
      foreignKey = @ForeignKey(name = "failed_tx_out_tx_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Tx tx;

  @Column(name = "index", nullable = false)
  @TxIndex
  private Short index;

  @Column(name = "address", nullable = false)
  private String address;

  @Column(name = "address_raw", nullable = false)
  private byte[] addressRaw;

  @Column(name = "address_has_script", nullable = false)
  private Boolean addressHasScript;

  @Column(name = "payment_cred", length = 56)
  @Hash28Type
  private String paymentCred;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "stake_address_id",
      foreignKey = @ForeignKey(name = "collateral_tx_out_stake_address_id_fkey"))
  @EqualsAndHashCode.Exclude
  private StakeAddress stakeAddress;

  @Column(name = "value", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger value;

  @Column(name = "data_hash", length = 64)
  @Hash32Type
  private String dataHash;

  @Column(name = "multi_assets_descr", nullable = false, length = 65535)
  private String multiAssetsDescr;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "inline_datum_id",
      foreignKey = @ForeignKey(name = "collateral_tx_out_inline_datum_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Datum inlineDatum;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "reference_script_id",
      foreignKey = @ForeignKey(name = "collateral_tx_out_reference_script_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Script referenceScript;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    FailedTxOut that = (FailedTxOut) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
