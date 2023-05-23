package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;
import org.cardanofoundation.rosetta.common.ledgersync.BlockVersion;
import org.cardanofoundation.rosetta.common.ledgersync.SoftwareVersion;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronBlockExtraData<T> {

  private BlockVersion blockVersion;
  private SoftwareVersion softwareVersion;
  private T attributes;
  private String extraProof;
}
