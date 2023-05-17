package org.cardanofoundation.rosetta.crawler.projection;

import java.sql.Timestamp;
import lombok.ToString;

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