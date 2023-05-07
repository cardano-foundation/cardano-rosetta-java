package org.cardanofoundation.rosetta.common.ledgersync.kafka;


import lombok.Getter;
import lombok.Setter;
import org.cardanofoundation.rosetta.common.ledgersync.Era;

@Getter
@Setter
public abstract class AbstractBlock implements CommonBlock {
  protected int cborSize;
  private Era eraType;
  private long blockTime;
  private int network;
  private boolean rollback;


}
