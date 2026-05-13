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
 * Read-only entity mapping to yaci-store assets-ext CIP-26 offchain metadata table
 * ({@code cip26_metadata}). Logo is now a column on this table; the previous separate
 * {@code ft_offchain_logo} table was collapsed into this row in the V2 assets-ext schema.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "cip26_metadata")
public class TokenMetadataEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(length = 120)
    private String subject;

    @Nullable
    @Column(length = 50)
    private String name;

    @Nullable
    @Column(length = 500)
    private String description;

    @Nullable
    @Column(length = 9)
    private String ticker;

    @Nullable
    @Column(length = 250)
    private String url;

    @Nullable
    private Long decimals;

    /**
     * Base64-encoded image, up to ~87 KB per CIP-26 spec. Always loaded eagerly — matches
     * upstream yaci-store entity. PostgreSQL TOAST keeps the column off main heap pages so
     * scans that ignore this field still pay no I/O cost.
     */
    @Nullable
    @Column(columnDefinition = "TEXT")
    private String logo;
}
