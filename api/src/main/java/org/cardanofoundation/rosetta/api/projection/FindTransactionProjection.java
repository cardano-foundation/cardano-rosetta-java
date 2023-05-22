package org.cardanofoundation.rosetta.api.projection;


import org.cardanofoundation.rosetta.common.entity.Tx;

/**
 * A Projection for the {@link Tx} entity
 */
public interface FindTransactionProjection {

  String getFee();

  byte[] getHash();

  byte[] getBlockHash();

  Long getBlockNo();

  Long getScriptSize();

  Long getSize();

  Boolean getValidContract();
}