package org.cardanofoundation.rosetta.yaciindexer.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transaction_size")
public class TransactionSizeEntity {

  @Column(name = "tx_hash")
  @Id
  private String txHash;

  @Column(name = "block_number")
  private long blockNumber;

  @Column(name = "size")
  private int size;

  @Column(name = "script_size")
  private int scriptSize;
}
