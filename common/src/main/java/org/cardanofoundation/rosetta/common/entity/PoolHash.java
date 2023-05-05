package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.sql.Timestamp;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "pool_hash", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pool_hash",
        columnNames = {"hash_raw"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@DynamicUpdate
public class PoolHash extends BaseEntity {

  @Column(name = "hash_raw", nullable = false, length = 56)
  @Hash28Type
  private String hashRaw;

  @Column(name = "view", nullable = false)
  private String view;

  @Column(name = "created_at")
  private Timestamp createdAt;
  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @Column(name = "updated_at")
  private Timestamp updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    PoolHash poolHash = (PoolHash) o;
    return id != null && Objects.equals(id, poolHash.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
