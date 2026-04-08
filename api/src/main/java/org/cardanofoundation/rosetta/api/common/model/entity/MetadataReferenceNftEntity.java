package org.cardanofoundation.rosetta.api.common.model.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

/**
 * Read-only entity mapping to yaci-store assets-ext CIP-68 metadata reference NFT table.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "metadata_reference_nft")
@IdClass(MetadataReferenceNftId.class)
public class MetadataReferenceNftEntity {

    @Id
    @Column(name = "policy_id", length = 56)
    private String policyId;

    @Id
    @Column(name = "asset_name", length = 255)
    private String assetName;

    @Id
    private Long slot;

    @Column(name = "label")
    private Integer label;

    @Nullable
    private String name;

    @Nullable
    private String description;

    @Nullable
    private String ticker;

    @Nullable
    private String url;

    @Nullable
    private Long decimals;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "TEXT")
    private String logo;

    @Nullable
    private Long version;
}
