package org.cardanofoundation.rosetta.api.block.model.entity;

import java.math.BigInteger;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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
