package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
  private List<Tran> transactions;
  private String poolDeposit;

}
