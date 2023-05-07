package org.cardanofoundation.rosetta.common.ledgersync.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.cardanofoundation.rosetta.common.ledgersync.Block;
import org.cardanofoundation.rosetta.common.ledgersync.Era;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronEbBlock;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronMainBlock;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = CommonBlock.TYPE)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Block.class, name = Block.TYPE),
    @JsonSubTypes.Type(value = ByronEbBlock.class, name = ByronEbBlock.TYPE),
    @JsonSubTypes.Type(value = ByronMainBlock.class, name = ByronMainBlock.TYPE),
})
public interface  CommonBlock {

  String TYPE = "type";

  @JsonIgnore
  String getType();

  @JsonIgnore
  byte[] getBlockHash();

  @JsonIgnore
  long getSlot();

  @JsonIgnore
  long getBlockNumber();

  @JsonIgnore
  String getPreviousHash();

  Era getEraType();

  boolean isRollback();

  void setRollback(boolean rollback);

  void setCborSize(int cborSize);

  void setBlockTime(long blockTime);

  void setNetwork(int network);

  void setEraType(Era type);


}
