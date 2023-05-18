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
    private Long number;
    private Long createdAt;
    private String previousBlockHash;
    private Long previousBlockNumber;
    private Long transactionsCount;
    private String createdBy;
    private Integer size;
    private Integer epochNo;
    private Long slotNo;
}
