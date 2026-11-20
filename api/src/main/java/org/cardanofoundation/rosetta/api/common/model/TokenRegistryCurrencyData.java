package org.cardanofoundation.rosetta.api.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

/**
 * Domain object representing token registry currency metadata.
 * This is an immutable domain object separate from view/serialization concerns.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRegistryCurrencyData {

    @Nullable
    private String policyId;

    @Nullable
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
    private LogoData logo;

    @Nullable
    private BigDecimal version;

    /**
     * Number of decimal places for the token. Always populated — {@link
     * org.cardanofoundation.rosetta.api.common.service.TokenQueryService} guarantees a
     * non-null value (defaulting to {@code 0} when neither CIP-26 nor CIP-68 provides an
     * explicit count). Unlike the other metadata fields on this record, {@code decimals} is
     * NOT gated by {@code TOKEN_REGISTRY_ENABLED}: it flows through to the wire
     * unconditionally because Rosetta's {@code Currency.decimals} is a mandatory
     * cross-chain field and clients expect it to always be present.
     */
    @Nonnull
    private Integer decimals;

    /**
     * Domain object representing logo information.
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoData {

        @Nullable
        private LogoFormat format;

        @Nullable
        private String value;
    }

    /**
     * Logo format enum for domain layer.
     */
    public enum LogoFormat {
        BASE64,
        URL
    }
}