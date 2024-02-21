package org.cardanofoundation.rosetta.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.common.model.AddressBalanceEntity;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressBalanceDTO {
    private String address;
    private String unit;
    private Long slot;
    private BigInteger quantity;
    private String policy;


    public static AddressBalanceDTO fromEntity(AddressBalanceEntity addressBalanceEntity) {
        return AddressBalanceDTO.builder()
                .address(addressBalanceEntity.getAddress())
                .unit(addressBalanceEntity.getUnit())
                .slot(addressBalanceEntity.getSlot())
                .quantity(addressBalanceEntity.getQuantity())
                .policy(addressBalanceEntity.getPolicy())
                .build();
    }
}
