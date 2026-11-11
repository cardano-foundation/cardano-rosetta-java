package org.cardanofoundation.rosetta.api.common.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

/**
 * Read-only copy of yaci-store assets-ext {@code Cip68Metadata} (table {@code cip68_metadata}),
 * from yaci-store {@code release/2.0.x}: CIP-68 reference NFT metadata for labels 222/333/444.
 * <p>
 * The api and yaci-indexer modules are separate, so the api module keeps its own copies of the
 * yaci-store entities it reads. This copy keeps the full primary key and the columns Rosetta
 * serves; the column mappings follow the upstream entity. Not mapped:
 * {@code media_type}, {@code datum}, {@code properties}, {@code validation_errors},
 * {@code last_synced_at}.
 * <p>
 * Values that break CIP-68 are stored as written by assets-ext (and reported in
 * {@code validation_errors}), so callers must not assume the CIP limits hold.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "cip68_metadata")
@IdClass(Cip68MetadataId.class)
public class Cip68MetadataEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "policy_id", length = 56, nullable = false)
    private String policyId;

    /** Reference NFT asset name (prefix {@code 000643b0}), hex-encoded. */
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "asset_name", length = 64, nullable = false)
    private String assetName;

    @Id
    @EqualsAndHashCode.Include
    @Column(nullable = false)
    private Long slot;

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "tx_hash", length = 64, nullable = false)
    private String txHash;

    @Column(name = "tx_index", nullable = false)
    private Integer txIndex;

    @Column(nullable = false)
    private Integer label;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String name;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String description;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String ticker;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String url;

    /** CIP-68 decimals; a value outside the CIP range is stored as written (see {@code TokenQueryServiceImpl}). */
    @Nullable
    private Long decimals;

    /** URL ({@code http(s)://}, {@code ipfs://}, {@code ar://}) or base64 / {@code data:} URI. */
    @Nullable
    @Column(columnDefinition = "TEXT")
    private String logo;

    /** CIP-68 {@code image}; served as the logo of a rich fungible token (444), which has no {@code logo}. */
    @Nullable
    @Column(columnDefinition = "TEXT")
    private String image;

    /** CIP-68 metadata version; nullable since assets-ext {@code V0_1700_2} (an invalid version is reported, not dropped). */
    @Nullable
    private Long version;

}
