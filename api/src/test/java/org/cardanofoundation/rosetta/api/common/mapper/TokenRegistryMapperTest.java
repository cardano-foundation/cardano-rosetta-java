package org.cardanofoundation.rosetta.api.common.mapper;

import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.LogoType;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenRegistryMapper Tests")
class TokenRegistryMapperTest {

    private TokenRegistryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TokenRegistryMapperImpl();
    }

    @Nested
    @DisplayName("toCurrencyMetadataResponse")
    class ToCurrencyMetadataResponseTests {

        @Test
        @DisplayName("Should map all fields from TokenRegistryCurrencyData")
        void shouldMapAllFields() {
            TokenRegistryCurrencyData data = TokenRegistryCurrencyData.builder()
                    .policyId("abc123")
                    .subject("abc123deadbeef")
                    .name("HOSKY")
                    .description("Dog token on Cardano")
                    .ticker("HOSKY")
                    .url("https://hosky.io")
                    .version(BigDecimal.valueOf(2))
                    .decimals(0)
                    .build();

            CurrencyMetadataResponse result = mapper.toCurrencyMetadataResponse(data);

            assertThat(result.getPolicyId()).isEqualTo("abc123");
            assertThat(result.getSubject()).isEqualTo("abc123deadbeef");
            assertThat(result.getName()).isEqualTo("HOSKY");
            assertThat(result.getDescription()).isEqualTo("Dog token on Cardano");
            assertThat(result.getTicker()).isEqualTo("HOSKY");
            assertThat(result.getUrl()).isEqualTo("https://hosky.io");
            assertThat(result.getVersion()).isEqualTo(BigDecimal.valueOf(2));
        }

        @Test
        @DisplayName("Should handle null data")
        void shouldHandleNull() {
            CurrencyMetadataResponse result = mapper.toCurrencyMetadataResponse(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle all null fields")
        void shouldHandleAllNullFields() {
            TokenRegistryCurrencyData data = TokenRegistryCurrencyData.builder().build();

            CurrencyMetadataResponse result = mapper.toCurrencyMetadataResponse(data);

            assertThat(result).isNotNull();
            assertThat(result.getPolicyId()).isNull();
            assertThat(result.getName()).isNull();
            assertThat(result.getLogo()).isNull();
        }

        @Test
        @DisplayName("Should not map decimals to response")
        void shouldNotMapDecimals() {
            TokenRegistryCurrencyData data = TokenRegistryCurrencyData.builder()
                    .decimals(6)
                    .build();

            CurrencyMetadataResponse result = mapper.toCurrencyMetadataResponse(data);

            // decimals is intentionally not in CurrencyMetadataResponse — it goes in Currency.decimals
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("mapLogoData")
    class MapLogoDataTests {

        @Test
        @DisplayName("Should return null when logoData is null")
        void shouldReturnNullForNullInput() {
            LogoType result = mapper.mapLogoData(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should map BASE64 format correctly")
        void shouldMapBase64Format() {
            TokenRegistryCurrencyData.LogoData logoData = TokenRegistryCurrencyData.LogoData.builder()
                    .format(TokenRegistryCurrencyData.LogoFormat.BASE64)
                    .value("iVBORw0KGgoAAAANSUhEUg==")
                    .build();

            LogoType result = mapper.mapLogoData(logoData);

            assertThat(result).isNotNull();
            assertThat(result.getFormat()).isEqualTo(LogoType.FormatEnum.BASE64);
            assertThat(result.getValue()).isEqualTo("iVBORw0KGgoAAAANSUhEUg==");
        }

        @Test
        @DisplayName("Should map URL format correctly")
        void shouldMapUrlFormat() {
            TokenRegistryCurrencyData.LogoData logoData = TokenRegistryCurrencyData.LogoData.builder()
                    .format(TokenRegistryCurrencyData.LogoFormat.URL)
                    .value("ipfs://QmHash123")
                    .build();

            LogoType result = mapper.mapLogoData(logoData);

            assertThat(result).isNotNull();
            assertThat(result.getFormat()).isEqualTo(LogoType.FormatEnum.URL);
            assertThat(result.getValue()).isEqualTo("ipfs://QmHash123");
        }

        @Test
        @DisplayName("Should handle null format")
        void shouldHandleNullFormat() {
            TokenRegistryCurrencyData.LogoData logoData = TokenRegistryCurrencyData.LogoData.builder()
                    .format(null)
                    .value("somedata")
                    .build();

            LogoType result = mapper.mapLogoData(logoData);

            assertThat(result).isNotNull();
            assertThat(result.getFormat()).isNull();
            assertThat(result.getValue()).isEqualTo("somedata");
        }

        @Test
        @DisplayName("Should handle null value")
        void shouldHandleNullValue() {
            TokenRegistryCurrencyData.LogoData logoData = TokenRegistryCurrencyData.LogoData.builder()
                    .format(TokenRegistryCurrencyData.LogoFormat.BASE64)
                    .value(null)
                    .build();

            LogoType result = mapper.mapLogoData(logoData);

            assertThat(result).isNotNull();
            assertThat(result.getFormat()).isEqualTo(LogoType.FormatEnum.BASE64);
            assertThat(result.getValue()).isNull();
        }
    }

    @Nested
    @DisplayName("End-to-end: logo in full mapping")
    class LogoEndToEndTests {

        @Test
        @DisplayName("CIP-26 BASE64 logo should appear in CurrencyMetadataResponse")
        void cip26LogoShouldAppearInResponse() {
            TokenRegistryCurrencyData data = TokenRegistryCurrencyData.builder()
                    .policyId("abc")
                    .name("Token")
                    .logo(TokenRegistryCurrencyData.LogoData.builder()
                            .format(TokenRegistryCurrencyData.LogoFormat.BASE64)
                            .value("base64png")
                            .build())
                    .build();

            CurrencyMetadataResponse result = mapper.toCurrencyMetadataResponse(data);

            assertThat(result.getLogo()).isNotNull();
            assertThat(result.getLogo().getFormat()).isEqualTo(LogoType.FormatEnum.BASE64);
            assertThat(result.getLogo().getValue()).isEqualTo("base64png");
        }

        @Test
        @DisplayName("CIP-68 URL logo should appear in CurrencyMetadataResponse")
        void cip68LogoShouldAppearInResponse() {
            TokenRegistryCurrencyData data = TokenRegistryCurrencyData.builder()
                    .policyId("abc")
                    .name("Token")
                    .logo(TokenRegistryCurrencyData.LogoData.builder()
                            .format(TokenRegistryCurrencyData.LogoFormat.URL)
                            .value("https://example.com/logo.png")
                            .build())
                    .build();

            CurrencyMetadataResponse result = mapper.toCurrencyMetadataResponse(data);

            assertThat(result.getLogo()).isNotNull();
            assertThat(result.getLogo().getFormat()).isEqualTo(LogoType.FormatEnum.URL);
            assertThat(result.getLogo().getValue()).isEqualTo("https://example.com/logo.png");
        }

        @Test
        @DisplayName("No logo should result in null logo in response")
        void noLogoShouldResultInNull() {
            TokenRegistryCurrencyData data = TokenRegistryCurrencyData.builder()
                    .policyId("abc")
                    .name("Token")
                    .build();

            CurrencyMetadataResponse result = mapper.toCurrencyMetadataResponse(data);

            assertThat(result.getLogo()).isNull();
        }
    }
}
