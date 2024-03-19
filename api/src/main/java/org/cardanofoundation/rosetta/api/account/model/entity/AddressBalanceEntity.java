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
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "address_balance")
@IdClass(AddressBalanceId.class)
@DynamicUpdate
public class AddressBalanceEntity extends BlockAwareEntity {

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

  //Only set if address doesn't fit in ownerAddr field. Required for few Byron Era addr
  @Column(name = "addr_full")
  private String addrFull;

  @Column(name = "policy")
  private String policy;

  @Column(name = "asset_name")
  private String assetName;

  @Column(name = "payment_credential")
  private String paymentCredential;

  @Column(name = "stake_address")
  private String stakeAddress;

  @Column(name = "block_hash")
  private String blockHash;

  @Column(name = "epoch")
  private Integer epoch;

}
