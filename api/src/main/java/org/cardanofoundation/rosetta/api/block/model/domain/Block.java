package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Block {

  private String hash;
  private Long number;
  private Long createdAt;
  private String previousBlockHash;
  private Long previousBlockNumber;
  private Long transactionsCount;
  private String createdBy;
  private Integer size;
  private Integer epochNo;
  private Long slotNo;
  private List<Transaction> transactions;

  public static Block fromBlock(BlockEntity block) {
    return Block.builder()
        .number(block.getNumber())
        .hash(block.getHash())
        .createdAt(block.getBlockTime())
        .previousBlockHash(block.getPrev() != null ? block.getPrev().getHash() : "")
        .previousBlockNumber(block.getPrev() != null ? block.getPrev().getNumber() : -1)
        .transactionsCount(block.getNoOfTxs())
        .size(Math.toIntExact(block.getBlockBodySize()))
        .createdBy(
            block.getIssuerVkey()) // TODO probably need to change this, in typescript rosetta there is something like Pool-[HASH]
        .epochNo(block.getEpochNumber())
        .slotNo(block.getSlot())
        .transactions(
            block.getTransactions().stream().map(txnEntity -> Transaction.fromTx(txnEntity))
                .toList())
        .build();
  }
}
