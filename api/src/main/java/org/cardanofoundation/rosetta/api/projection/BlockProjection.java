package org.cardanofoundation.rosetta.api.projection;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;

public interface BlockProjection {

  Long getNumber();

  String getHash();

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  Timestamp getCreatedAt();

  String getPreviousBlockHash();

  Long getPreviousBlockNumber();

  Long getTransactionsCount();

  String getCreatedBy();

  Integer getSize();

  Integer getEpochNo();

  Long getSlotNo();
}