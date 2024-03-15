package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.rosetta.api.block.model.entity.PoolRetirementEntity;

@Data
@Builder
public class PoolRetirement {

    private String txHash;

    private long certIndex;

    private String poolId;

    private Integer epoch;

    private Long slot;

    private String blockHash;

    public static PoolRetirement fromEntity(PoolRetirementEntity entity) {
        return PoolRetirement.builder()
                .txHash(entity.getTxHash())
                .certIndex(entity.getCertIndex())
                .poolId(entity.getPoolId())
                .epoch(entity.getEpoch())
                .slot(entity.getSlot())
                .blockHash(entity.getBlockHash())
                .build();
    }
}
