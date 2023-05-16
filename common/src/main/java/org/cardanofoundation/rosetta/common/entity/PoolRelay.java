package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Entity
@Table(name = "pool_relay", uniqueConstraints = {
    @UniqueConstraint(name = "unique_pool_relay",
        columnNames = {"update_id", "ipv4", "ipv6", "dns_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PoolRelay extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "update_id", nullable = false,
      foreignKey = @ForeignKey(name = "pool_relay_update_id_fkey"))
  @EqualsAndHashCode.Exclude
  private PoolUpdate poolUpdate;

  @Column(name = "ipv4")
  private String ipv4;

  @Column(name = "ipv6")
  private String ipv6;

  @Column(name = "dns_name")
  private String dnsName;

  @Column(name = "dns_srv_name")
  private String dnsSrvName;

  @Column(name = "port")
  private Integer port;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    PoolRelay poolRelay = (PoolRelay) o;
    return id != null && Objects.equals(id, poolRelay.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
