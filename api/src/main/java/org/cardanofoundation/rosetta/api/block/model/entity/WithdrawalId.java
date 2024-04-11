package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import java.io.Serializable;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class WithdrawalId implements Serializable {
    @Column(name = "address")
    private String address;

    @Column(name = "tx_hash")
    private String txHash;
}
