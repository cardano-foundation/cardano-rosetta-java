package org.cardanofoundation.rosetta.common.mapper;

import org.cardanofoundation.rosetta.api.common.mapper.TokenRegistryMapper;
import org.cardanofoundation.rosetta.api.common.mapper.TokenRegistryMapperImpl;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.LogoType;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DataMapper Tests")
class DataMapperTest {

    private DataMapper dataMapper;

    @BeforeEach
    void setUp() {
        TokenRegistryMapper tokenRegistryMapper = new TokenRegistryMapperImpl();
        dataMapper = new DataMapper(tokenRegistryMapper);
    }

    @Nested
    @DisplayName("mapAmount")
    class MapAmountTests {

        @Test
        @DisplayName("Should create ADA amount when symbol is null")
        void shouldCreateAdaAmountWhenSymbolNull() {
            Amount result = dataMapper.mapAmount("2000000", null, null, null);

            assertThat(result.getValue()).isEqualTo("2000000");
            assertThat(result.getCurrency().getSymbol()).isEqualTo("ADA");
            assertThat(result.getCurrency().getDecimals()).isEqualTo(6);
            assertThat(result.getCurrency().getMetadata()).isNull();
        }

        @Test
        @DisplayName("Should create native token amount with symbol and decimals")
        void shouldCreateNativeTokenAmount() {
            TokenRegistryCurrencyData metadata = TokenRegistryCurrencyData.builder()
                    .policyId("abc123")
                    .name("HOSKY")
                    .decimals(0)
                    .build();

            Amount result = dataMapper.mapAmount("1000000", "484f534b59", 0, metadata);

            assertThat(result.getValue()).isEqualTo("1000000");
            assertThat(result.getCurrency().getSymbol()).isEqualTo("484f534b59");
            assertThat(result.getCurrency().getDecimals()).isEqualTo(0);
            assertThat(result.getCurrency().getMetadata()).isNotNull();
            assertThat(result.getCurrency().getMetadata().getPolicyId()).isEqualTo("abc123");
            assertThat(result.getCurrency().getMetadata().getName()).isEqualTo("HOSKY");
        }

        @Test
        @DisplayName("Should include logo metadata from CIP-26 BASE64")
        void shouldIncludeCip26Logo() {
            TokenRegistryCurrencyData metadata = TokenRegistryCurrencyData.builder()
                    .policyId("abc123")
                    .name("Token")
                    .logo(TokenRegistryCurrencyData.LogoData.builder()
                            .format(TokenRegistryCurrencyData.LogoFormat.BASE64)
                            .value("iVBORw0KGgo=")
                            .build())
                    .build();

            Amount result = dataMapper.mapAmount("100", "deadbeef", 6, metadata);

            assertThat(result.getCurrency().getMetadata().getLogo()).isNotNull();
            assertThat(result.getCurrency().getMetadata().getLogo().getFormat()).isEqualTo(LogoType.FormatEnum.BASE64);
        }

        @Test
        @DisplayName("Should include logo metadata from CIP-68 URL")
        void shouldIncludeCip68Logo() {
            TokenRegistryCurrencyData metadata = TokenRegistryCurrencyData.builder()
                    .policyId("abc123")
                    .name("Token")
                    .logo(TokenRegistryCurrencyData.LogoData.builder()
                            .format(TokenRegistryCurrencyData.LogoFormat.URL)
                            .value("ipfs://QmHash")
                            .build())
                    .build();

            Amount result = dataMapper.mapAmount("100", "deadbeef", 6, metadata);

            assertThat(result.getCurrency().getMetadata().getLogo()).isNotNull();
            assertThat(result.getCurrency().getMetadata().getLogo().getFormat()).isEqualTo(LogoType.FormatEnum.URL);
            assertThat(result.getCurrency().getMetadata().getLogo().getValue()).isEqualTo("ipfs://QmHash");
        }

        @Test
        @DisplayName("Should handle null metadata gracefully")
        void shouldHandleNullMetadata() {
            Amount result = dataMapper.mapAmount("500", "aabb", 2, null);

            assertThat(result.getValue()).isEqualTo("500");
            assertThat(result.getCurrency().getSymbol()).isEqualTo("aabb");
            assertThat(result.getCurrency().getDecimals()).isEqualTo(2);
            assertThat(result.getCurrency().getMetadata()).isNull();
        }

        @Test
        @DisplayName("Should pass version through to metadata response")
        void shouldPassVersionThrough() {
            TokenRegistryCurrencyData metadata = TokenRegistryCurrencyData.builder()
                    .policyId("abc")
                    .version(BigDecimal.valueOf(3))
                    .build();

            Amount result = dataMapper.mapAmount("100", "ff", 0, metadata);

            assertThat(result.getCurrency().getMetadata().getVersion()).isEqualTo(BigDecimal.valueOf(3));
        }
    }

    @Nested
    @DisplayName("mapValue")
    class MapValueTests {

        @Test
        @DisplayName("Should negate value when spent")
        void shouldNegateWhenSpent() {
            assertThat(dataMapper.mapValue("1000000", true)).isEqualTo("-1000000");
        }

        @Test
        @DisplayName("Should return value as-is when not spent")
        void shouldReturnAsIsWhenNotSpent() {
            assertThat(dataMapper.mapValue("1000000", false)).isEqualTo("1000000");
        }
    }
}
