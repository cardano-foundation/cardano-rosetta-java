package org.cardanofoundation.rosetta.common.ledgersync.byron;

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
public class ByronBlockHead implements
    ByronHead<ByronBlockProof, ByronBlockCons, ByronBlockExtraData<String>> {

  private long protocolMagic;
  private byte[] blockHash;
  private String prevBlock;
  private ByronBlockProof bodyProof;
  private ByronBlockCons consensusData;
  private ByronBlockExtraData<String> extraData;
}
