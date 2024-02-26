package org.cardanofoundation.rosetta.api.model.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.cardanofoundation.rosetta.common.model.StakeAddressBalanceEntity;

import java.math.BigInteger;

@Data
@Builder
public class StakeAddressBalanceDTO {
    private String address;
    private Long slot;
    private BigInteger quantity;
    private String stakeCredential;
    private String blockHash;
    private Integer epoch;


    public static StakeAddressBalanceDTO fromEntity(StakeAddressBalanceEntity entity) {
        return StakeAddressBalanceDTO.builder()
                .address(entity.getAddress())
                .slot(entity.getSlot())
                .quantity(entity.getQuantity())
                .stakeCredential(entity.getStakeCredential())
                .blockHash(entity.getBlockHash())
                .epoch(entity.getEpoch())
                .build();
    }
}
