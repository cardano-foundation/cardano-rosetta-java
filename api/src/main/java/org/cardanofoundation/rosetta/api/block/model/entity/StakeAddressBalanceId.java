package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class StakeAddressBalanceId implements Serializable {
    @Column(name = "address")
    private String address;
    @Column(name = "slot")
    private Long slot;
}
