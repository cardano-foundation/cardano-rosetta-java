package org.cardanofoundation.rosetta.api.block.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invalid_transaction")
public class InvalidTransactionEntity {
    @Id
    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "slot")
    private Long slot;

    @Column(name = "block_hash")
    private String blockHash;
}
