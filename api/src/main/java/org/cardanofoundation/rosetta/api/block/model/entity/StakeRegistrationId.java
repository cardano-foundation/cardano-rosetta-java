package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class StakeRegistrationId implements Serializable {

    @Column(name = "tx_hash")
    private String txHash;
    @Column(name = "cert_index")
    private long certIndex;
}
