package org.cardanofoundation.rosetta.crawler.projection;

import java.sql.Timestamp;

public interface BlockProjection {
        int getNumber();
        byte[] getHash();
        Timestamp getCreatedAt();
        byte[] getPreviousBlockHash();
        int getPreviousBlockNumber();
        int getTransactionsCount();
        String getCreatedBy();
        int getSize();
        int getEpochNo();
        String getSlotNo();
}