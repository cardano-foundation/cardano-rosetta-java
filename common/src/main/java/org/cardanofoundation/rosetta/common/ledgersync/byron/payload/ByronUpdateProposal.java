package org.cardanofoundation.rosetta.common.ledgersync.byron.payload;

import lombok.*;
import org.cardanofoundation.rosetta.common.ledgersync.SoftwareVersion;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronBlockVersion;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronBlockVersionMod;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronUpdateData;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronUpdateProposal {
  private ByronBlockVersion blockVersion;
  private ByronBlockVersionMod blockVersionMod;
  private SoftwareVersion softwareVersion;
  private Map<String, ByronUpdateData> data;
  private String attributes;
  private String from;
  private String signature;
}
