package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockIdentifierExtended {

  private String hash;
  private Long number;
  private Long blockTimeInSeconds;
  private Long slot;

}
