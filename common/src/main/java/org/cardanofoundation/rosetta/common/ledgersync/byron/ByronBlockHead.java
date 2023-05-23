package org.cardanofoundation.rosetta.common.ledgersync.byron;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class ByronBlockHead implements
    ByronHead<ByronBlockProof, ByronBlockCons, ByronBlockExtraData<String>> {

  private long protocolMagic;
  private String blockHash;
  private String prevBlock;
  private ByronBlockProof bodyProof;
  private ByronBlockCons consensusData;
  private ByronBlockExtraData<String> extraData;
}
