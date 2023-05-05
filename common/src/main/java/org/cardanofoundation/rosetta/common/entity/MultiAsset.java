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
import org.cardanofoundation.rosetta.common.validation.Asset32Type;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.hibernate.Hibernate;

@Entity
@Table(name = "multi_asset", uniqueConstraints = {
    @UniqueConstraint(name = "unique_multi_asset",
        columnNames = {"policy", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MultiAsset extends BaseEntity {

  @Column(name = "policy", nullable = false, length = 56)
  @Hash28Type
  private byte[] policy;

  @Column(name = "name", nullable = false, length = 64)
  @Asset32Type
//  @Convert(converter = ByteConverter.class)
  private byte[] name;

  @Column(name = "fingerprint", nullable = false)
  private String fingerprint;

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
    MultiAsset that = (MultiAsset) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
