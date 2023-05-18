package org.cardanofoundation.rosetta.common.entity;

import java.sql.Timestamp;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.TxIndex;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "tx_in", uniqueConstraints = {
    @UniqueConstraint(name = "unique_txin",
        columnNames = {"tx_out_id", "tx_out_index"}
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TxIn extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "tx_in_id", nullable = false,
      foreignKey = @ForeignKey(name = "tx_in_tx_in_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Tx txInput;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "tx_out_id", nullable = false,
      foreignKey = @ForeignKey(name = "tx_in_tx_out_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Tx txOut;

  @Column(name = "tx_in_id", updatable = false, insertable = false)
  private Long txInputId;

  @Column(name = "tx_out_id", updatable = false, insertable = false)
  private Long txOutputId;

  @Column(name = "tx_out_index", nullable = false)
  @TxIndex
  private Short txOutIndex;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "redeemer_id",
      foreignKey = @ForeignKey(name = "tx_in_redeemer_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Redeemer redeemer;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    TxIn txIn = (TxIn) o;
    return id != null && Objects.equals(id, txIn.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
