package org.cardanofoundation.rosetta.api.model.dto;

import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.rosetta.common.model.DelegationEntity;

@Data
@Builder
public class DelegationDTO {

    private String txHash;

    private long certIndex;

    private String poolId;

    private String credential;

    private String address;

    private Integer epoch;

    private Long slot;

    private String blockHash;

    public static DelegationDTO fromEntity(DelegationEntity entity) {
        return DelegationDTO.builder()
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