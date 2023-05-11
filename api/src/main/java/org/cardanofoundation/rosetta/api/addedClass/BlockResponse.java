package org.cardanofoundation.rosetta.api.addedClass;

import javax.xml.crypto.Data;
import java.util.Date;

public interface BlockResponse {
    byte[] getHash();
    Long getNumber();
    Date getCreatedAt();
    String getPreviousBlockHash();
    Long getPreviousBlockNumber();
    Long getTransactionsCount();
    String getCreatedBy();
    Integer getSize();
    Integer getEpochNo();
    Long getSlotNo();

}
