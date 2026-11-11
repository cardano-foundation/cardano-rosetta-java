package org.cardanofoundation.rosetta.api.common.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

/**
 * Read-only copy of yaci-store assets-ext {@code Cip26Metadata} (table {@code cip26_metadata}),
 * from yaci-store {@code release/2.0.x}.
 * <p>
 * The api and yaci-indexer modules are separate, so the api module keeps its own copies of the
 * yaci-store entities it reads. This copy is trimmed to the columns Rosetta serves; the column
 * mappings follow the upstream entity. Not mapped: {@code policy}, {@code updated},
 * {@code updated_by}, {@code properties}, {@code validation_errors}, {@code last_synced_at}.
 * <p>
 * Values that break CIP-26 are stored as written by assets-ext (and reported in
 * {@code validation_errors}), so callers must not assume the CIP limits hold.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "cip26_metadata")
public class Cip26MetadataEntity {

    /** Subject = policyId (28 bytes) + optional assetName (0-32 bytes), hex-encoded, lower case. */
    @Id
    @EqualsAndHashCode.Include
    @Column(length = 120)
    private String subject;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String name;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String ticker;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String url;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String description;

    /** CIP-26 decimals; a value outside the CIP range is stored as written (see {@code TokenQueryServiceImpl}). */
    @Nullable
    private Long decimals;

    /**
     * Base64-encoded image, up to ~87 KB per CIP-26. PostgreSQL TOAST keeps the column off main
     * heap pages, so scans that ignore this field pay no I/O cost.
     */
    @Nullable
    @Column(columnDefinition = "TEXT")
    private String logo;

}
