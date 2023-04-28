package org.cardanofoundation.rosetta.common.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Hash32Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "datum", uniqueConstraints = {
    @UniqueConstraint(name = "unique_datum",
        columnNames = {"hash"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Datum extends BaseEntity {

  @Column(name = "hash", nullable = false, length = 64)
  @Hash32Type
  private String hash;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "tx_id", nullable = false,
      foreignKey = @ForeignKey(name = "datum_tx_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Tx tx;

  @Column(name = "value", length = 65535)
  private String value;

  @Column(name = "bytes")
  private byte[] bytes;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Datum datum = (Datum) o;
    return id != null && Objects.equals(id, datum.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
