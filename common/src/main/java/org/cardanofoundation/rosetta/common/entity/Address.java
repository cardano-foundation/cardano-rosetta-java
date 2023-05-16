package org.cardanofoundation.rosetta.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.validation.Word128Type;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

import java.math.BigInteger;
import java.util.Objects;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Where(clause = "is_deleted = false")
public class Address extends BaseEntity {

  @Column(name = "address", nullable = false, length = 65535)
  private String address;

  @Column(name = "tx_count")
  private Long txCount;

  @Column(name = "balance", nullable = false, precision = 39)
  @Word128Type
  @Digits(integer = 39, fraction = 0)
  private BigInteger balance;

  @Column(name = "address_has_script", nullable = false)
  private Boolean addressHasScript;

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "stake_address_id",
      foreignKey = @ForeignKey(name = "address_stake_address_id_fkey"))
  private StakeAddress stakeAddress;

  @Column(name = "stake_address_id", updatable = false, insertable = false)
  private Long stakeAddressId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Address address = (Address) o;
    return id != null && Objects.equals(id, address.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}