package org.cardanofoundation.rosetta.api.model.dto;

import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.rosetta.common.model.PoolRetirementEntity;

@Data
@Builder
public class PoolRetirementDTO {

    private String txHash;

    private long certIndex;

    private String poolId;

    private Integer epoch;

    private Long slot;

    private String blockHash;

    public static PoolRetirementDTO fromEntity(PoolRetirementEntity entity) {
        return PoolRetirementDTO.builder()
                .txHash(entity.getTxHash())
                .certIndex(entity.getCertIndex())
                .poolId(entity.getPoolId())
                .epoch(entity.getEpoch())
                .slot(entity.getSlot())
                .blockHash(entity.getBlockHash())
                .build();
    }
}
