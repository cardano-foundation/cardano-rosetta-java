package org.cardanofoundation.rosetta.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Objects;

@Entity
@Table(name = "reserved_pool_ticker", uniqueConstraints = {
    @UniqueConstraint(name = "unique_reserved_pool_ticker",
        columnNames = {"name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ReservedPoolTicker extends BaseEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "pool_hash", nullable = false, length = 56)
  @Hash28Type
  private String poolHash;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    ReservedPoolTicker that = (ReservedPoolTicker) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
