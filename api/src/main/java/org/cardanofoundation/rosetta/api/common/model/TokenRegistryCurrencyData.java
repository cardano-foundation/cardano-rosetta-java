package org.cardanofoundation.rosetta.api.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Nullable
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