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
public class ByronEbHead implements ByronHead<String, ByronEbBlockCons, String> {

  private long protocolMagic;
  private String blockHash;
  private String prevBlock;
  private String bodyProof;
  private ByronEbBlockCons consensusData;
  private String extraData;
}
