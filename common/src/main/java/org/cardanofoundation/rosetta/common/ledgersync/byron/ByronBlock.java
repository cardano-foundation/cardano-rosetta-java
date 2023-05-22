package org.cardanofoundation.rosetta.common.ledgersync.byron;

import org.cardanofoundation.rosetta.common.ledgersync.kafka.CommonBlock;

public interface ByronBlock extends CommonBlock {

  <T extends ByronHead> T getHeader(); //No Sonar

  <T> T getBody();
}
