package org.cardanofoundation.rosetta.api.common.model.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MetadataReferenceNftId implements Serializable {

    private String policyId;
    private String assetName;
    private Long slot;
}
