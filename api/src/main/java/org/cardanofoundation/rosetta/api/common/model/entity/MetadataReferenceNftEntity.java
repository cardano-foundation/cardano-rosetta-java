package org.cardanofoundation.rosetta.api.common.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

/**
 * Read-only entity mapping to yaci-store assets-ext CIP-68 on-chain reference NFT metadata
 * table ({@code cip68_metadata}). Stores rows for label 222/333/444; rosetta queries the
 * latest row per {@code (policy_id, asset_name)} pair regardless of label.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "cip68_metadata")
@IdClass(MetadataReferenceNftId.class)
public class MetadataReferenceNftEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "policy_id", length = 56, nullable = false)
    private String policyId;

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "asset_name", length = 64, nullable = false)
    private String assetName;

    @Id
    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private Long slot;

    @Column(nullable = false)
    private Integer label;

    @Nullable
    @Column(length = 255)
    private String name;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String description;

    @Nullable
    @Column(length = 32)
    private String ticker;

    @Nullable
    @Column(length = 250)
    private String url;

    @Nullable
    private Long decimals;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String logo;

    @Column(nullable = false)
    private Long version;
}
