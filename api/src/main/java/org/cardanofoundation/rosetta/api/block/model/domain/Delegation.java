package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.rosetta.api.block.model.entity.DelegationEntity;

@Data
@Builder
public class Delegation {

    private String txHash;

    private long certIndex;

    private String poolId;

    private String credential;

    private String address;

    private Integer epoch;

    private Long slot;

    private String blockHash;

    public static Delegation fromEntity(DelegationEntity entity) {
        return Delegation.builder()
                .txHash(entity.getTxHash())
                .certIndex(entity.getCertIndex())
                .poolId(entity.getPoolId())
                .credential(entity.getCredential())
                .address(entity.getAddress())
                .epoch(entity.getEpoch())
                .slot(entity.getSlot())
                .blockHash(entity.getBlockHash())
                .build();
    }
}