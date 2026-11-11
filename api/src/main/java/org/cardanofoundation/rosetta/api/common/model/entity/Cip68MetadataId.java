package org.cardanofoundation.rosetta.api.common.model.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Composite key of {@link Cip68MetadataEntity}, matching yaci-store assets-ext
 * {@code Cip68MetadataId}: {@code (policy_id, asset_name, slot, tx_hash)}.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Cip68MetadataId implements Serializable {

    private String policyId;
    private String assetName;
    private Long slot;
    private String txHash;

}
