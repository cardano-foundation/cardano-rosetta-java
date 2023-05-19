package org.cardanofoundation.rosetta.crawler.projection;

import java.sql.Timestamp;

public interface BlockProjection {

  Long getNumber();

  String getHash();

  Timestamp getCreatedAt();

  String getPreviousBlockHash();

  Long getPreviousBlockNumber();

  Long getTransactionsCount();

  String getCreatedBy();

  Integer getSize();

  Integer getEpochNo();

  Long getSlotNo();
}