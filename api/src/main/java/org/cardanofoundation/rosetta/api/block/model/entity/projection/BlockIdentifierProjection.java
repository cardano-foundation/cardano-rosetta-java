package org.cardanofoundation.rosetta.api.block.model.entity.projection;

public interface BlockIdentifierProjection {
  String getHash();
  Long getNumber();
  Long getBlockTimeInSeconds();

}
