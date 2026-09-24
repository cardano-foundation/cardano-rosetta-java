package org.cardanofoundation.rosetta.api.common.model.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MetadataReferenceNftId implements Serializable {

    private String policyId;
    private String assetName;
    private Long slot;
}
