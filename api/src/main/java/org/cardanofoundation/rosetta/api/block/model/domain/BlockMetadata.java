package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BlockMetadata {

  private Long transactionsCount;
  private String createdBy;
  private Integer size;
  private Integer epochNo;
  private Long slotNo;
}
