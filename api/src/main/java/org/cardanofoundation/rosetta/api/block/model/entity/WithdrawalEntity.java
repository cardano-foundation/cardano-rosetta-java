package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
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