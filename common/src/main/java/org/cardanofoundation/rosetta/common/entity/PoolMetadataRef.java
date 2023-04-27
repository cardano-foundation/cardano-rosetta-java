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
@Table(name = "pool_metadata_ref", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pool_metadata_ref",
        columnNames = {"pool_id", "url", "hash"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PoolMetadataRef extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "pool_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_metadata_ref_pool_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolHash poolHash;

  @Column(name = "url", nullable = false)
  private String url;

  @Column(name = "hash", nullable = false, length = 64)
  @Hash32Type
  private String hash;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "registered_tx_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_metadata_ref_registered_tx_id_fkey"))
  @EqualsAndHashCode.Exclude
  private Tx registeredTx;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    PoolMetadataRef that = (PoolMetadataRef) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
