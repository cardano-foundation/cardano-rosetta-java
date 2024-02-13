package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.cardanofoundation.rosetta.common.validation.Word31Type;
import org.cardanofoundation.rosetta.common.validation.Word64Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(name = "tx", uniqueConstraints = {
        @UniqueConstraint(name = "unique_tx",
                columnNames = {"hash"}
        )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SuperBuilder(toBuilder = true)
public class Tx implements Serializable {

  @Column(name = "tx_hash", nullable = false)
  @Id
//  @Hash32Type
  private String hash;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "block_hash", nullable = false)
  @EqualsAndHashCode.Exclude
  private Block block;

  @Column(name = "out_sum", precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger outSum;

  @Column(name = "fee", precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger fee;

  @Column(name = "deposit")
  private Long deposit;

  @Column(name = "size")
  @Word31Type
  private Integer size;

  @Column(name = "invalid_before", precision = 20)
  @Word64Type
  @Digits(integer = 20, fraction = 0)
  private BigInteger invalidBefore;

  @Column(name = "invalid_hereafter", precision = 20)
  @Word64Type
  @Digits(integer = 20, fraction = 0)
  private BigInteger invalidHereafter;

  @Column(name = "valid_contract")
  private Boolean validContract;

  @Column(name = "script_size")
  @Word31Type
  private Integer scriptSize;
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Tx tx = (Tx) o;
    return hash != null && Objects.equals(hash, tx.hash);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  public void addScriptSize(int size) {
    if (this.size == null) {
      this.size = 0;
    }
    this.size += size;
  }

}
