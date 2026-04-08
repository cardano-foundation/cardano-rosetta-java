package org.cardanofoundation.rosetta.api.common.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

/**
 * Read-only entity mapping to yaci-store assets-ext CIP-26 offchain metadata table.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ft_offchain_metadata")
public class TokenMetadataEntity {

    @Id
    private String subject;

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
}
