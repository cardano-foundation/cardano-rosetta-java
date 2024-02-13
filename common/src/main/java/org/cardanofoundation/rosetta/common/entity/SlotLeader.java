package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "slot_leader", uniqueConstraints = {
    @UniqueConstraint(name = "unique_slot_leader",
        columnNames = {"hash"}
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SlotLeader implements Serializable {

  @Column(name = "hash", nullable = false, length = 56)
  @Hash28Type
  @Id
  private String hash;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pool_hash_id",
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
  @EqualsAndHashCode.Exclude
  private PoolHash poolHash;

  @Column(name = "description", nullable = false, length = 65535)
  private String description;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    SlotLeader that = (SlotLeader) o;
    return hash != null && Objects.equals(hash, that.hash);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
