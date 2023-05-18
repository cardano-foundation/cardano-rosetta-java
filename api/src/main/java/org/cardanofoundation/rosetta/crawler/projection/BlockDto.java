package org.cardanofoundation.rosetta.crawler.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockDto {

    private String hash;
    private long number;
    private long createdAt;
    private String previousBlockHash;
    private long previousBlockNumber;
    private int transactionsCount;
    private String createdBy;
    private int size;
    private int epochNo;
    private String slotNo;
}
