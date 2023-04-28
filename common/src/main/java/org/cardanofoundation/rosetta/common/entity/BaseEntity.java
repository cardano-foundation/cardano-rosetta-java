package org.cardanofoundation.rosetta.common.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {

  @Id
  @Column(name = "id", nullable = false, insertable = false, updatable = false)
  @GeneratedValue(generator = "seq_generator")
  @GenericGenerator(name = "seq_generator", strategy = "sequence", parameters = {
      @Parameter(name = SequenceStyleGenerator.CONFIG_PREFER_SEQUENCE_PER_ENTITY, value = "true"),
      @Parameter(name = SequenceStyleGenerator.CONFIG_SEQUENCE_PER_ENTITY_SUFFIX, value = "_ID_SEQ")
  })
  protected Long id;

/*  @Column(name = "created_at")
  @CreatedDate
  protected Timestamp createdAt;

  @Column(name = "updated_at")
  @LastModifiedDate
  protected Timestamp updatedAt;*/

//  @Column(name = "is_deleted")
//  @ColumnDefault("false")
//  protected Boolean isDeleted = false;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    BaseEntity that = (BaseEntity) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
