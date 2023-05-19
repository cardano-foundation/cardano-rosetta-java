package org.cardanofoundation.rosetta.api.projection;

import java.sql.Timestamp;

public interface BlockProjection {
        Long getNumber();
        byte[] getHash();
        Timestamp getCreatedAt();
        byte[] getPreviousBlockHash();
        Long getPreviousBlockNumber();
        Long getTransactionsCount();
        String getCreatedBy();
        Integer getSize();
        Integer getEpochNo();
        Long getSlotNo();
}