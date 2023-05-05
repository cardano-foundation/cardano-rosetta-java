package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Digits;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Lovelace;
import org.cardanofoundation.rosetta.common.validation.Word128Type;
import org.cardanofoundation.rosetta.common.validation.Word31Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "epoch", uniqueConstraints = {
    @UniqueConstraint(name = "unique_epoch",
        columnNames = {"no"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@DynamicUpdate
public class Epoch extends BaseEntity {

  @Column(name = "out_sum", nullable = false, precision = 39)
  @Word128Type
  @Digits(integer = 39, fraction = 0)
  private BigInteger outSum;

  @Column(name = "fees", nullable = false, precision = 20)
  @Lovelace
  @Digits(integer = 20, fraction = 0)
  private BigInteger fees;

  @Column(name = "tx_count", nullable = false)
  @Word31Type
  private Integer txCount;

  @Column(name = "blk_count", nullable = false)
  @Word31Type
  private Integer blkCount;

  @Column(name = "no", nullable = false)
  @Word31Type
  private Integer no;

  @Column(name = "start_time")
  private Timestamp startTime;

  @Column(name = "end_time")
  private Timestamp endTime;





/*  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "epoch_no" ,insertable =false, updatable = false)
  private Set<Block> blocks;*/

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Epoch epoch = (Epoch) o;
    return id != null && Objects.equals(id, epoch.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
