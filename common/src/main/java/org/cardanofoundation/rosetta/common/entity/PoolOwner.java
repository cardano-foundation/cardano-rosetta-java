package org.cardanofoundation.rosetta.common.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "pool_owner", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pool_owner",
        columnNames = {"addr_id", "pool_update_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PoolOwner extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "addr_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_owner_addr_id_fkey"))
  @EqualsAndHashCode.Exclude
  private StakeAddress stakeAddress;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "pool_update_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_owner_pool_update_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolUpdate poolUpdate;

  @Column(name = "pool_update_id", updatable = false, insertable = false)
  private Long poolUpdateId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    PoolOwner poolOwner = (PoolOwner) o;
    return id != null && Objects.equals(id, poolOwner.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
