package org.cardanofoundation.rosetta.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Hash32Type;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Objects;

@Entity
@Table(name = "cost_model", uniqueConstraints = {
    @UniqueConstraint(name = "unique_cost_model",
        columnNames = {"hash"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CostModel extends BaseEntity {

  @Column(name = "costs", nullable = false, length = 65535)
  private String costs;


  @Column(name = "hash", nullable = false, length = 64)
  @Hash32Type
  private String hash;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    CostModel costModel = (CostModel) o;

    if(Objects.isNull(id) && Objects.nonNull(hash)){
      return hash.equals(costModel.getHash());
    }

    return id != null && Objects.equals(id, costModel.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
