package org.cardanofoundation.rosetta.common.ledgersync.byron;

import org.cardanofoundation.rosetta.common.ledgersync.BlockVersion;
import org.cardanofoundation.rosetta.common.ledgersync.SoftwareVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
