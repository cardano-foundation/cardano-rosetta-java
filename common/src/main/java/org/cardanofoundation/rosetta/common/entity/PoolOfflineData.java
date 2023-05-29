package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
@Table(name = "pool_offline_data", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pool_offline_data",
        columnNames = {"pool_id", "hash"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PoolOfflineData extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "pool_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_offline_data_pool_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolHash pool;

  @Column(name = "ticker_name", nullable = false)
  private String tickerName;

  @Column(name = "pool_name", nullable = false)
  private String poolName;

  @Column(name = "hash", nullable = false, length = 64)
  //@Hash32Type
  private String hash;

  @Column(name = "json", nullable = false, length = 65535)
  private String json;

  @Column(name = "bytes")
  private byte[] bytes;

  @Column(name = "pmr_id", insertable = false, updatable = false)
  private Long pmrId;

  @Column(name = "pool_id", insertable = false, updatable = false)
  private Long poolId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "pmr_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_offline_data_pmr_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolMetadataRef poolMetadataRef;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    PoolOfflineData that = (PoolOfflineData) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
