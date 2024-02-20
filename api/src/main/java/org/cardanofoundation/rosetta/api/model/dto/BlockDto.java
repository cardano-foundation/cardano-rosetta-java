package org.cardanofoundation.rosetta.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.rest.TransactionDto;
import org.cardanofoundation.rosetta.common.model.BlockEntity;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockDto {

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
  private List<TransactionDto> transactions;

  public static BlockDto fromBlock(BlockEntity block) {
      return BlockDto.builder()
            .number(block.getNumber())
            .hash(block.getHash())
            .createdAt(block.getBlockTime())
            .previousBlockHash(block.getPrev().getHash())
            .previousBlockNumber(block.getPrev().getNumber())
            .transactionsCount(block.getNoOfTxs())
            .size(Math.toIntExact(block.getBlockBodySize()))
            .epochNo(block.getEpochNumber())
            .slotNo(block.getSlot())
            .transactions(block.getTransactions().stream().map(txnEntity -> TransactionDto.fromTx(txnEntity)).toList())
            .build();
  }
}
