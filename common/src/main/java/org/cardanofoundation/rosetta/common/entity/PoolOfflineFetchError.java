package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Word63Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "pool_offline_fetch_error", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pool_offline_fetch_error",
        columnNames = {"pool_id", "fetch_time", "retry_count"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PoolOfflineFetchError extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "pool_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_offline_fetch_error_pool_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolHash poolHash;

  @Column(name = "fetch_time", nullable = false)
  private Timestamp fetchTime;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "pmr_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_offline_fetch_error_pmr_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolMetadataRef poolMetadataRef;

  @Column(name = "fetch_error", nullable = false, length = 65535)
  private String fetchError;

  @Column(name = "retry_count", nullable = false)
  @Word63Type
  private Integer retryCount;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    PoolOfflineFetchError that = (PoolOfflineFetchError) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
