package org.cardanofoundation.rosetta.api.account.model.entity;

import java.math.BigInteger;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.hibernate.annotations.DynamicUpdate;

import org.cardanofoundation.rosetta.api.block.model.entity.BlockAwareEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "address_balance")
@IdClass(AddressBalanceId.class)
@DynamicUpdate
public class AddressBalanceEntity {

  @Id
  @Column(name = "address")
  private String address;

  @Id
  @Column(name = "unit")
  private String unit;

  @Id
  @Column(name = "slot")
  private Long slot;

  @Column(name = "quantity")
  private BigInteger quantity;

  @Column(name = "block")
  private Long blockNumber;
}
