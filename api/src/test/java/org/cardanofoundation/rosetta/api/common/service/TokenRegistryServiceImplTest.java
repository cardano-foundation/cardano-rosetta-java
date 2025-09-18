package org.cardanofoundation.rosetta.api.common.service;

import org.cardanofoundation.rosetta.api.common.model.Asset;
import org.cardanofoundation.rosetta.client.TokenRegistryHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.TokenMetadata;
import org.cardanofoundation.rosetta.client.model.domain.TokenProperty;
import org.cardanofoundation.rosetta.client.model.domain.TokenPropertyNumber;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.LogoType;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenRegistryServiceImpl Tests")
class TokenRegistryServiceImplTest {

    @Mock
    private TokenRegistryHttpGateway tokenRegistryHttpGateway;

    private TokenRegistryServiceImpl tokenRegistryService;

    private static final String POLICY_ID = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3";
    private static final String ASSET_NAME = "TestToken";
    private static final String SUBJECT = POLICY_ID + "54657374546f6b656e"; // hex encoding of "TestToken"

    @BeforeEach
    void setUp() {
        tokenRegistryService = new TokenRegistryServiceImpl(tokenRegistryHttpGateway);
    }

    @Nested
    @DisplayName("getTokenMetadataBatch Tests")
    class GetTokenMetadataBatchTests {

        @Test
        @DisplayName("Should return empty map when assets set is empty")
        void shouldReturnEmptyMapForEmptyAssets() {
            // given
            Set<Asset> emptyAssets = Set.of();

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(emptyAssets);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(tokenRegistryHttpGateway);
        }

        @Test
        @DisplayName("Should return fallback metadata when gateway returns no data")
        void shouldReturnFallbackMetadataWhenNoGatewayData() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.empty()));

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            assertThat(result).hasSize(1);
            CurrencyMetadataResponse metadata = result.get(asset);
            assertThat(metadata).isNotNull();
            assertThat(metadata.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(metadata.getSubject()).isNull();
            assertThat(metadata.getName()).isNull();
            assertThat(metadata.getDescription()).isNull();
            assertThat(metadata.getDecimals()).isNull();
        }

        @Test
        @DisplayName("Should return fallback metadata when gateway returns null entry")
        void shouldReturnFallbackMetadataWhenGatewayReturnsNull() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            Map<String, Optional<TokenSubject>> gatewayResponse = new HashMap<>();
            gatewayResponse.put(SUBJECT, null);
            
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(gatewayResponse);

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            assertThat(result).hasSize(1);
            CurrencyMetadataResponse metadata = result.get(asset);
            assertThat(metadata).isNotNull();
            assertThat(metadata.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(metadata.getSubject()).isNull();
            assertThat(metadata.getName()).isNull();
        }

        @Test
        @DisplayName("Should return fallback metadata when subject not found in gateway response")
        void shouldReturnFallbackMetadataWhenSubjectNotFound() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of()); // Empty map - subject not found

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            assertThat(result).hasSize(1);
            CurrencyMetadataResponse metadata = result.get(asset);
            assertThat(metadata).isNotNull();
            assertThat(metadata.getPolicyId()).isEqualTo(POLICY_ID);
        }

        @Test
        @DisplayName("Should extract complete metadata when gateway returns full token data")
        void shouldExtractCompleteMetadataWhenFullDataAvailable() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            TokenSubject tokenSubject = createCompleteTokenSubject();
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            assertThat(result).hasSize(1);
            CurrencyMetadataResponse metadata = result.get(asset);
            
            assertThat(metadata.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(metadata.getSubject()).isEqualTo(SUBJECT);
            assertThat(metadata.getName()).isEqualTo("Test Token");
            assertThat(metadata.getDescription()).isEqualTo("Test Description");
            assertThat(metadata.getTicker()).isEqualTo("TST");
            assertThat(metadata.getUrl()).isEqualTo("https://test.com");
            assertThat(metadata.getDecimals()).isEqualTo(6);
            assertThat(metadata.getVersion()).isEqualTo(BigDecimal.valueOf(1L));
            
            assertThat(metadata.getLogo()).isNotNull();
            assertThat(metadata.getLogo().getFormat()).isEqualTo(LogoType.FormatEnum.BASE64);
            assertThat(metadata.getLogo().getValue()).isEqualTo("base64logo");
        }

        @Test
        @DisplayName("Should handle multiple assets correctly")
        void shouldHandleMultipleAssetsCorrectly() {
            // given
            Asset asset1 = createAsset(POLICY_ID, ASSET_NAME);
            Asset asset2 = createAsset("policy2", "Asset2");
            Set<Asset> assets = Set.of(asset1, asset2);
            
            String subject2 = "policy2" + "417373657432"; // hex encoding of "Asset2"
            TokenSubject tokenSubject1 = createCompleteTokenSubject();
            
            Map<String, Optional<TokenSubject>> gatewayResponse = Map.of(
                SUBJECT, Optional.of(tokenSubject1),
                subject2, Optional.empty() // No data for second asset
            );
            
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(gatewayResponse);

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            assertThat(result).hasSize(2);
            
            // First asset should have complete metadata
            CurrencyMetadataResponse metadata1 = result.get(asset1);
            assertThat(metadata1.getName()).isEqualTo("Test Token");
            assertThat(metadata1.getPolicyId()).isEqualTo(POLICY_ID);
            
            // Second asset should have fallback metadata only
            CurrencyMetadataResponse metadata2 = result.get(asset2);
            assertThat(metadata2.getPolicyId()).isEqualTo("policy2");
            assertThat(metadata2.getName()).isNull();
        }

        @Test
        @DisplayName("Should handle minimal metadata correctly")
        void shouldHandleMinimalMetadataCorrectly() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            TokenSubject tokenSubject = createMinimalTokenSubject();
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            assertThat(result).hasSize(1);
            CurrencyMetadataResponse metadata = result.get(asset);
            
            assertThat(metadata.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(metadata.getSubject()).isEqualTo(SUBJECT);
            assertThat(metadata.getName()).isEqualTo("Minimal Token");
            assertThat(metadata.getDescription()).isEqualTo("Minimal Description");
            assertThat(metadata.getDecimals()).isEqualTo(0); // Default value
            
            // Optional fields should be null
            assertThat(metadata.getTicker()).isNull();
            assertThat(metadata.getUrl()).isNull();
            assertThat(metadata.getLogo()).isNull();
            assertThat(metadata.getVersion()).isNull();
        }
    }

    @Nested
    @DisplayName("Logo Conversion Tests")
    class LogoConversionTests {

        @Test
        @DisplayName("Should convert CIP_26 logo to BASE64 format")
        void shouldConvertCip26LogoToBase64() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            TokenSubject tokenSubject = createTokenSubjectWithLogo("CIP_26", "hexdata123");
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            CurrencyMetadataResponse metadata = result.get(asset);
            assertThat(metadata.getLogo()).isNotNull();
            assertThat(metadata.getLogo().getFormat()).isEqualTo(LogoType.FormatEnum.BASE64);
            assertThat(metadata.getLogo().getValue()).isEqualTo("hexdata123");
        }

        @Test
        @DisplayName("Should convert CIP_68 logo to URL format")
        void shouldConvertCip68LogoToUrl() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            TokenSubject tokenSubject = createTokenSubjectWithLogo("CIP_68", "https://example.com/logo.png");
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            CurrencyMetadataResponse metadata = result.get(asset);
            assertThat(metadata.getLogo()).isNotNull();
            assertThat(metadata.getLogo().getFormat()).isEqualTo(LogoType.FormatEnum.URL);
            assertThat(metadata.getLogo().getValue()).isEqualTo("https://example.com/logo.png");
        }

        @Test
        @DisplayName("Should handle case insensitive CIP standards")
        void shouldHandleCaseInsensitiveCipStandards() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            TokenSubject tokenSubject = createTokenSubjectWithLogo("cip_26", "data");
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            CurrencyMetadataResponse metadata = result.get(asset);
            assertThat(metadata.getLogo().getFormat()).isEqualTo(LogoType.FormatEnum.BASE64);
        }

        @Test
        @DisplayName("Should return null format for unknown CIP standard")
        void shouldReturnNullFormatForUnknownCipStandard() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            TokenSubject tokenSubject = createTokenSubjectWithLogo("UNKNOWN", "data");
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            CurrencyMetadataResponse metadata = result.get(asset);
            assertThat(metadata.getLogo()).isNotNull();
            assertThat(metadata.getLogo().getFormat()).isNull();
            assertThat(metadata.getLogo().getValue()).isEqualTo("data");
        }

        @Test
        @DisplayName("Should return null logo when logo property is null")
        void shouldReturnNullLogoWhenPropertyIsNull() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            TokenSubject tokenSubject = createTokenSubjectWithLogo(null, null);
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            CurrencyMetadataResponse metadata = result.get(asset);
            assertThat(metadata.getLogo()).isNull();
        }

        @Test
        @DisplayName("Should create logo with null value when logo value is null")
        void shouldCreateLogoWithNullValueWhenValueIsNull() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            TokenProperty logoProperty = mock(TokenProperty.class);
            when(logoProperty.getValue()).thenReturn(null);
            when(logoProperty.getSource()).thenReturn("CIP_26");
            
            TokenSubject tokenSubject = createTokenSubjectWithCustomLogo(logoProperty);
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            CurrencyMetadataResponse metadata = result.get(asset);
            assertThat(metadata.getLogo()).isNotNull();
            assertThat(metadata.getLogo().getFormat()).isEqualTo(LogoType.FormatEnum.BASE64);
            assertThat(metadata.getLogo().getValue()).isNull();
        }

    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandlingTests {

        @Test
        @DisplayName("Should handle gateway throwing exception gracefully")
        void shouldHandleGatewayExceptionGracefully() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenThrow(new RuntimeException("Gateway error"));

            // when & then
            assertThrows(RuntimeException.class, () -> tokenRegistryService.getTokenMetadataBatch(assets));
        }

        @Test
        @DisplayName("Should handle null decimals gracefully")
        void shouldHandleNullDecimalsGracefully() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            TokenSubject tokenSubject = createTokenSubjectWithNullDecimals();
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            CurrencyMetadataResponse metadata = result.get(asset);
            assertThat(metadata.getDecimals()).isEqualTo(0); // Default value
        }

        @Test
        @DisplayName("Should verify correct subject generation for asset")
        void shouldVerifyCorrectSubjectGenerationForAsset() {
            // given
            Asset asset = createAsset(POLICY_ID, ASSET_NAME);
            Set<Asset> assets = Set.of(asset);
            
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of());

            // when
            tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            verify(tokenRegistryHttpGateway).getTokenMetadataBatch(eq(Set.of(SUBJECT)));
        }

        @Test
        @DisplayName("Should handle large batch of assets")
        void shouldHandleLargeBatchOfAssets() {
            // given
            Set<Asset> assets = new HashSet<>();
            Map<String, Optional<TokenSubject>> gatewayResponse = new HashMap<>();
            
            for (int i = 0; i < 100; i++) {
                String policyId = "policy" + String.format("%02d", i);
                String assetName = "Asset" + i;
                Asset asset = createAsset(policyId, assetName);
                assets.add(asset);
                
                String subject = asset.toSubject();
                gatewayResponse.put(subject, Optional.empty());
            }
            
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(gatewayResponse);

            // when
            Map<Asset, CurrencyMetadataResponse> result = tokenRegistryService.getTokenMetadataBatch(assets);

            // then
            assertThat(result).hasSize(100);
            result.values().forEach(metadata -> {
                assertThat(metadata).isNotNull();
                assertThat(metadata.getPolicyId()).isNotNull();
            });
        }
    }

    // Helper methods
    private Asset createAsset(String policyId, String assetName) {
        return Asset.builder()
                .policyId(policyId)
                .assetName(assetName)
                .build();
    }

    private TokenSubject createCompleteTokenSubject() {
        TokenSubject tokenSubject = mock(TokenSubject.class);
        when(tokenSubject.getSubject()).thenReturn(SUBJECT);
        
        TokenMetadata tokenMetadata = mock(TokenMetadata.class);
        when(tokenSubject.getMetadata()).thenReturn(tokenMetadata);
        
        // Mandatory fields
        TokenProperty name = mock(TokenProperty.class);
        when(name.getValue()).thenReturn("Test Token");
        when(tokenMetadata.getName()).thenReturn(name);
        
        TokenProperty description = mock(TokenProperty.class);
        when(description.getValue()).thenReturn("Test Description");
        when(tokenMetadata.getDescription()).thenReturn(description);
        
        // Optional fields
        TokenProperty ticker = mock(TokenProperty.class);
        when(ticker.getValue()).thenReturn("TST");
        when(tokenMetadata.getTicker()).thenReturn(ticker);
        
        TokenProperty url = mock(TokenProperty.class);
        when(url.getValue()).thenReturn("https://test.com");
        when(tokenMetadata.getUrl()).thenReturn(url);
        
        TokenProperty logo = mock(TokenProperty.class);
        when(logo.getValue()).thenReturn("base64logo");
        when(logo.getSource()).thenReturn("CIP_26");
        when(tokenMetadata.getLogo()).thenReturn(logo);
        
        TokenPropertyNumber version = mock(TokenPropertyNumber.class);
        when(version.getValue()).thenReturn(1L);
        when(tokenMetadata.getVersion()).thenReturn(version);
        
        TokenPropertyNumber decimals = mock(TokenPropertyNumber.class);
        when(decimals.getValue()).thenReturn(6L);
        when(tokenMetadata.getDecimals()).thenReturn(decimals);
        
        return tokenSubject;
    }

    private TokenSubject createMinimalTokenSubject() {
        TokenSubject tokenSubject = mock(TokenSubject.class);
        when(tokenSubject.getSubject()).thenReturn(SUBJECT);
        
        TokenMetadata tokenMetadata = mock(TokenMetadata.class);
        when(tokenSubject.getMetadata()).thenReturn(tokenMetadata);
        
        // Only mandatory fields
        TokenProperty name = mock(TokenProperty.class);
        when(name.getValue()).thenReturn("Minimal Token");
        when(tokenMetadata.getName()).thenReturn(name);
        
        TokenProperty description = mock(TokenProperty.class);
        when(description.getValue()).thenReturn("Minimal Description");
        when(tokenMetadata.getDescription()).thenReturn(description);
        
        // Optional fields are null
        when(tokenMetadata.getTicker()).thenReturn(null);
        when(tokenMetadata.getUrl()).thenReturn(null);
        when(tokenMetadata.getLogo()).thenReturn(null);
        when(tokenMetadata.getVersion()).thenReturn(null);
        when(tokenMetadata.getDecimals()).thenReturn(null);
        
        return tokenSubject;
    }

    private TokenSubject createTokenSubjectWithLogo(String source, String value) {
        TokenSubject tokenSubject = mock(TokenSubject.class);
        when(tokenSubject.getSubject()).thenReturn(SUBJECT);
        
        TokenMetadata tokenMetadata = mock(TokenMetadata.class);
        when(tokenSubject.getMetadata()).thenReturn(tokenMetadata);
        
        TokenProperty name = mock(TokenProperty.class);
        when(name.getValue()).thenReturn("Test Token");
        when(tokenMetadata.getName()).thenReturn(name);
        
        TokenProperty description = mock(TokenProperty.class);
        when(description.getValue()).thenReturn("Test Description");
        when(tokenMetadata.getDescription()).thenReturn(description);
        
        if (source != null && value != null) {
            TokenProperty logo = mock(TokenProperty.class);
            when(logo.getValue()).thenReturn(value);
            when(logo.getSource()).thenReturn(source);
            when(tokenMetadata.getLogo()).thenReturn(logo);
        } else {
            when(tokenMetadata.getLogo()).thenReturn(null);
        }
        
        return tokenSubject;
    }

    private TokenSubject createTokenSubjectWithCustomLogo(TokenProperty logoProperty) {
        TokenSubject tokenSubject = mock(TokenSubject.class);
        when(tokenSubject.getSubject()).thenReturn(SUBJECT);
        
        TokenMetadata tokenMetadata = mock(TokenMetadata.class);
        when(tokenSubject.getMetadata()).thenReturn(tokenMetadata);
        
        TokenProperty name = mock(TokenProperty.class);
        when(name.getValue()).thenReturn("Test Token");
        when(tokenMetadata.getName()).thenReturn(name);
        
        TokenProperty description = mock(TokenProperty.class);
        when(description.getValue()).thenReturn("Test Description");
        when(tokenMetadata.getDescription()).thenReturn(description);
        
        when(tokenMetadata.getLogo()).thenReturn(logoProperty);
        
        return tokenSubject;
    }

    private TokenSubject createTokenSubjectWithNullDecimals() {
        TokenSubject tokenSubject = mock(TokenSubject.class);
        when(tokenSubject.getSubject()).thenReturn(SUBJECT);
        
        TokenMetadata tokenMetadata = mock(TokenMetadata.class);
        when(tokenSubject.getMetadata()).thenReturn(tokenMetadata);
        
        TokenProperty name = mock(TokenProperty.class);
        when(name.getValue()).thenReturn("Test Token");
        when(tokenMetadata.getName()).thenReturn(name);
        
        TokenProperty description = mock(TokenProperty.class);
        when(description.getValue()).thenReturn("Test Description");
        when(tokenMetadata.getDescription()).thenReturn(description);
        
        when(tokenMetadata.getDecimals()).thenReturn(null);
        
        return tokenSubject;
    }
}