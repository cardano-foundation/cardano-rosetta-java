package org.cardanofoundation.rosetta.api.block.model.entity;

import java.math.BigInteger;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "withdrawal")
@IdClass(WithdrawalId.class)
public class WithdrawalEntity {

  @Id
  @Column(name = "address")
  private String address;

  @Id
  @Column(name = "tx_hash")
  private String txHash;

  @Column(name = "amount")
  private BigInteger amount;
}
