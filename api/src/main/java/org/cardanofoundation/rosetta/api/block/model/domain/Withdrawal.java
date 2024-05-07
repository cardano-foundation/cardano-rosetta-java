package org.cardanofoundation.rosetta.api.block.model.domain;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Withdrawal {
  private String stakeAddress;
  private BigInteger amount;
}
