package org.cardanofoundation.rosetta.common.ledgersync.byron;

public interface ByronHead<T, S, U> {

  long getProtocolMagic();

  byte[] getBlockHash();

  String getPrevBlock();

  T getBodyProof();

  S getConsensusData();

  U getExtraData();
}
