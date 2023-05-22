package org.cardanofoundation.rosetta.common.ledgersync.byron;

public interface ByronHead<T, S, U> {

  long getProtocolMagic();

  String getBlockHash();

  String getPrevBlock();

  T getBodyProof();

  S getConsensusData();

  U getExtraData();
}
