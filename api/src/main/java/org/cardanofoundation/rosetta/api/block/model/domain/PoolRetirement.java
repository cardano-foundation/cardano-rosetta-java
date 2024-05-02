package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // TODO saa: remove this and refactor tests
@NoArgsConstructor
@AllArgsConstructor
public class PoolRetirement {

  private String txHash;

  private int certIndex;

  private String poolId;

  private Integer epoch;

}
