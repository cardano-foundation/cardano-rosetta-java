package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.enumeration.ScriptType;
import org.cardanofoundation.rosetta.common.validation.Hash28Type;
import org.cardanofoundation.rosetta.common.validation.Word31Type;
import org.hibernate.Hibernate;

import java.util.Objects;

@Entity
@Table(name = "script", uniqueConstraints = {
    @UniqueConstraint(name = "unique_script", columnNames = {"hash"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Script extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tx_id", nullable = false,
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT, name = "none"))
  @EqualsAndHashCode.Exclude
  private Tx tx;

  @Column(name = "hash", nullable = false, length = 64)
  @Hash28Type
  private String hash;

  @Column(name = "type", nullable = false)
  private ScriptType type;

  //wip
  @Column(name = "json", length = 65535)
  private String json;

  @Column(name = "bytes", length = 10000)
  private byte[] bytes;

  @Column(name = "serialised_size")
  @Word31Type
  private Integer serialisedSize;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Script script = (Script) o;
    return id != null && Objects.equals(id, script.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
