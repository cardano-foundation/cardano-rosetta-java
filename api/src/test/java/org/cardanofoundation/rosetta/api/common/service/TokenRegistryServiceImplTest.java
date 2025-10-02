package org.cardanofoundation.rosetta.api.common.service;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
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
import org.openapitools.client.model.*;

import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;

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
    private static final String ASSET_NAME = "TestToken"; // Human-readable name
    private static final String ASSET_SYMBOL_HEX = "54657374546f6b656e"; // hex encoding of "TestToken"
    private static final String SUBJECT = POLICY_ID + ASSET_SYMBOL_HEX;

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
            Set<AssetFingerprint> emptyAssetFingerprints = Set.of();

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(emptyAssetFingerprints);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(tokenRegistryHttpGateway);
        }

        @Test
        @DisplayName("Should return fallback metadata when gateway returns no data")
        void shouldReturnFallbackMetadataWhenNoGatewayData() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.empty()));

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            assertThat(result).hasSize(1);
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);
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
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            Map<String, Optional<TokenSubject>> gatewayResponse = new HashMap<>();
            gatewayResponse.put(SUBJECT, null);

            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(gatewayResponse);

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            assertThat(result).hasSize(1);
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);
            assertThat(metadata).isNotNull();
            assertThat(metadata.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(metadata.getSubject()).isNull();
            assertThat(metadata.getName()).isNull();
        }

        @Test
        @DisplayName("Should return fallback metadata when subject not found in gateway response")
        void shouldReturnFallbackMetadataWhenSubjectNotFound() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of()); // Empty map - subject not found

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            assertThat(result).hasSize(1);
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);
            assertThat(metadata).isNotNull();
            assertThat(metadata.getPolicyId()).isEqualTo(POLICY_ID);
        }

        @Test
        @DisplayName("Should extract complete metadata when gateway returns full token data")
        void shouldExtractCompleteMetadataWhenFullDataAvailable() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            TokenSubject tokenSubject = createCompleteTokenSubject();
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            assertThat(result).hasSize(1);
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);

            assertThat(metadata.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(metadata.getSubject()).isEqualTo(SUBJECT);
            assertThat(metadata.getName()).isEqualTo("Test Token");
            assertThat(metadata.getDescription()).isEqualTo("Test Description");
            assertThat(metadata.getTicker()).isEqualTo("TST");
            assertThat(metadata.getUrl()).isEqualTo("https://test.com");
            assertThat(metadata.getDecimals()).isEqualTo(6);
            assertThat(metadata.getVersion()).isEqualTo(BigDecimal.valueOf(1L));

            assertThat(metadata.getLogo()).isNotNull();
            assertThat(metadata.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.BASE64);
            assertThat(metadata.getLogo().getValue()).isEqualTo("base64logo");
        }

        @Test
        @DisplayName("Should handle multiple assets correctly")
        void shouldHandleMultipleAssetsCorrectly() {
            // given
            AssetFingerprint assetFingerprint1 = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            AssetFingerprint assetFingerprint2 = createAsset("policy2", "Asset2");
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint1, assetFingerprint2);

            String subject2 = "policy2" + "417373657432"; // hex encoding of "Asset2"
            TokenSubject tokenSubject1 = createCompleteTokenSubject();

            Map<String, Optional<TokenSubject>> gatewayResponse = Map.of(
                SUBJECT, Optional.of(tokenSubject1),
                subject2, Optional.empty() // No data for second asset
            );

            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(gatewayResponse);

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            assertThat(result).hasSize(2);

            // First asset should have complete metadata
            TokenRegistryCurrencyData metadata1 = result.get(assetFingerprint1);
            assertThat(metadata1.getName()).isEqualTo("Test Token");
            assertThat(metadata1.getPolicyId()).isEqualTo(POLICY_ID);

            // Second asset should have fallback metadata only
            TokenRegistryCurrencyData metadata2 = result.get(assetFingerprint2);
            assertThat(metadata2.getPolicyId()).isEqualTo("policy2");
            assertThat(metadata2.getName()).isNull();
        }

        @Test
        @DisplayName("Should handle minimal metadata correctly")
        void shouldHandleMinimalMetadataCorrectly() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            TokenSubject tokenSubject = createMinimalTokenSubject();
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            assertThat(result).hasSize(1);
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);

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
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            TokenSubject tokenSubject = createTokenSubjectWithLogo("CIP_26", "hexdata123");
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);
            assertThat(metadata.getLogo()).isNotNull();
            assertThat(metadata.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.BASE64);
            assertThat(metadata.getLogo().getValue()).isEqualTo("hexdata123");
        }

        @Test
        @DisplayName("Should convert CIP_68 logo to URL format")
        void shouldConvertCip68LogoToUrl() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            TokenSubject tokenSubject = createTokenSubjectWithLogo("CIP_68", "https://example.com/logo.png");
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);
            assertThat(metadata.getLogo()).isNotNull();
            assertThat(metadata.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.URL);
            assertThat(metadata.getLogo().getValue()).isEqualTo("https://example.com/logo.png");
        }

        @Test
        @DisplayName("Should handle case insensitive CIP standards")
        void shouldHandleCaseInsensitiveCipStandards() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            TokenSubject tokenSubject = createTokenSubjectWithLogo("cip_26", "data");
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);
            assertThat(metadata.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.BASE64);
        }

        @Test
        @DisplayName("Should return null format for unknown CIP standard")
        void shouldReturnNullFormatForUnknownCipStandard() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            TokenSubject tokenSubject = createTokenSubjectWithLogo("UNKNOWN", "data");
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);
            assertThat(metadata.getLogo()).isNotNull();
            assertThat(metadata.getLogo().getFormat()).isNull();
            assertThat(metadata.getLogo().getValue()).isEqualTo("data");
        }

        @Test
        @DisplayName("Should return null logo when logo property is null")
        void shouldReturnNullLogoWhenPropertyIsNull() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            TokenSubject tokenSubject = createTokenSubjectWithLogo(null, null);
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);
            assertThat(metadata.getLogo()).isNull();
        }

        @Test
        @DisplayName("Should create logo with null value when logo value is null")
        void shouldCreateLogoWithNullValueWhenValueIsNull() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            TokenProperty logoProperty = mock(TokenProperty.class);
            when(logoProperty.getValue()).thenReturn(null);
            when(logoProperty.getSource()).thenReturn("CIP_26");

            TokenSubject tokenSubject = createTokenSubjectWithCustomLogo(logoProperty);
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);
            assertThat(metadata.getLogo()).isNotNull();
            assertThat(metadata.getLogo().getFormat()).isEqualTo(TokenRegistryCurrencyData.LogoFormat.BASE64);
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
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);
            
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenThrow(new RuntimeException("Gateway error"));

            // when & then
            assertThrows(RuntimeException.class, () -> tokenRegistryService.getTokenMetadataBatch(assetFingerprints));
        }

        @Test
        @DisplayName("Should handle null decimals gracefully")
        void shouldHandleNullDecimalsGracefully() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);

            TokenSubject tokenSubject = createTokenSubjectWithNullDecimals();
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(SUBJECT, Optional.of(tokenSubject)));

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            TokenRegistryCurrencyData metadata = result.get(assetFingerprint);
            assertThat(metadata.getDecimals()).isEqualTo(0); // Default value
        }

        @Test
        @DisplayName("Should verify correct subject generation for asset")
        void shouldVerifyCorrectSubjectGenerationForAsset() {
            // given
            AssetFingerprint assetFingerprint = createAsset(POLICY_ID, ASSET_SYMBOL_HEX);
            Set<AssetFingerprint> assetFingerprints = Set.of(assetFingerprint);
            
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of());

            // when
            tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            verify(tokenRegistryHttpGateway).getTokenMetadataBatch(eq(Set.of(SUBJECT)));
        }

        @Test
        @DisplayName("Should handle large batch of assets")
        void shouldHandleLargeBatchOfAssets() {
            // given
            Set<AssetFingerprint> assetFingerprints = new HashSet<>();
            Map<String, Optional<TokenSubject>> gatewayResponse = new HashMap<>();

            for (int i = 0; i < 100; i++) {
                String policyId = "policy" + String.format("%02d", i);
                String assetName = "Asset" + i;
                AssetFingerprint assetFingerprint = createAsset(policyId, assetName);
                assetFingerprints.add(assetFingerprint);

                String subject = assetFingerprint.toSubject();
                gatewayResponse.put(subject, Optional.empty());
            }

            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(gatewayResponse);

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.getTokenMetadataBatch(assetFingerprints);

            // then
            assertThat(result).hasSize(100);
            result.values().forEach(metadata -> {
                assertThat(metadata).isNotNull();
                assertThat(metadata.getPolicyId()).isNotNull();
            });
        }
    }

    @Nested
    @DisplayName("Asset Extraction from BlockTx Tests")
    class AssetFingerprintExtractionFromBlockTxTests {


        @Test
        @DisplayName("Should extract assets from inputs only")
        void shouldExtractAssetsFromInputsOnly() {
            // given
            BlockTx blockTx = createBlockTxWithInputs();

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).contains(
                AssetFingerprint.of("policy1", "token1"),
                AssetFingerprint.of("policy2", "token2")
            );
        }

        @Test
        @DisplayName("Should extract assets from outputs only")
        void shouldExtractAssetsFromOutputsOnly() {
            // given
            BlockTx blockTx = createBlockTxWithOutputs();

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).contains(
                AssetFingerprint.of("policy3", "token3"),
                AssetFingerprint.of("policy4", "token4")
            );
        }

        @Test
        @DisplayName("Should extract assets from both inputs and outputs")
        void shouldExtractAssetsFromBothInputsAndOutputs() {
            // given
            BlockTx blockTx = createBlockTxWithInputsAndOutputs();

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);

            // then
            assertThat(result).hasSize(4);
            assertThat(result).contains(
                AssetFingerprint.of("policy1", "token1"),
                AssetFingerprint.of("policy2", "token2"),
                AssetFingerprint.of("policy3", "token3"),
                AssetFingerprint.of("policy4", "token4")
            );
        }

        @Test
        @DisplayName("Should exclude lovelace from extraction")
        void shouldExcludeLovelaceFromExtraction() {
            // given
            BlockTx blockTx = createBlockTxWithLovelaceAndTokens();

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).contains(
                AssetFingerprint.of("policy1", "token1")
            );
        }

        @Test
        @DisplayName("Should handle empty inputs and outputs")
        void shouldHandleEmptyInputsAndOutputs() {
            // given
            BlockTx blockTx = BlockTx.builder()
                .inputs(List.of())
                .outputs(List.of())
                .build();

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null amounts in utxos")
        void shouldHandleNullAmountsInUtxos() {
            // given
            Utxo utxoWithNullAmounts = Utxo.builder().amounts(null).build();
            BlockTx blockTx = BlockTx.builder()
                .inputs(List.of(utxoWithNullAmounts))
                .build();

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Asset Extraction from Amounts Tests")
    class AssetFingerprintExtractionFromAmountsTests {


        @Test
        @DisplayName("Should return empty set when amounts is empty")
        void shouldReturnEmptySetWhenAmountsIsEmpty() {
            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromAmounts(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should extract native tokens only")
        void shouldExtractNativeTokensOnly() {
            // given
            List<Amt> amounts = List.of(
                createAmt(LOVELACE, null, LOVELACE), // ADA - should be excluded
                createAmt("token1", "policy1", "token1"),
                createAmt("token2", "policy2", "token2")
            );

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromAmounts(amounts);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).contains(
                AssetFingerprint.of("policy1", "token1"),
                AssetFingerprint.of("policy2", "token2")
            );
        }

        @Test
        @DisplayName("Should return empty set when only lovelace present")
        void shouldReturnEmptySetWhenOnlyLovelacePresent() {
            // given
            List<Amt> amounts = List.of(
                createAmt(LOVELACE, null, LOVELACE)
            );

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromAmounts(amounts);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Asset Extraction from BlockTransactions Tests")
    class AssetFingerprintExtractionFromBlockTransactionsTests {

        // Removed: shouldReturnEmptySetWhenTransactionsIsNull - parameter is @NotNull, null is not valid

        @Test
        @DisplayName("Should return empty set when transactions is empty")
        void shouldReturnEmptySetWhenTransactionsIsEmpty() {
            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTransactions(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should extract assets from operation metadata token bundles")
        void shouldExtractAssetsFromOperationMetadataTokenBundles() {
            // given
            List<BlockTransaction> transactions = List.of(createBlockTransactionWithTokenBundles());

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTransactions(transactions);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).contains(
                AssetFingerprint.of("policy1", "token1"),
                AssetFingerprint.of("policy2", "token2")
            );
        }

        @Test
        @DisplayName("Should extract assets from operation amount currency metadata")
        void shouldExtractAssetsFromOperationAmountCurrencyMetadata() {
            // given
            List<BlockTransaction> transactions = List.of(createBlockTransactionWithCurrencyMetadata());

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTransactions(transactions);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).contains(
                AssetFingerprint.of("policy1", "token1")
            );
        }

        // Removed: shouldHandleTransactionsWithNullOperations - operations field is @NotNull and initialized to ArrayList, never null

    }

    @Nested
    @DisplayName("Asset Extraction from Operations Tests")
    class AssetFingerprintExtractionFromOperationsTests {


        @Test
        @DisplayName("Should return empty set when operations is empty")
        void shouldReturnEmptySetWhenOperationsIsEmpty() {
            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromOperations(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should extract assets from token bundles in operation metadata")
        void shouldExtractAssetsFromTokenBundles() {
            // given
            List<Operation> operations = List.of(createOperationWithTokenBundle());

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromOperations(operations);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).contains(
                AssetFingerprint.of("policy1", "token1"),
                AssetFingerprint.of("policy1", "token2")
            );
        }

        @Test
        @DisplayName("Should extract assets from operation amount currency metadata")
        void shouldExtractAssetsFromAmountCurrencyMetadata() {
            // given
            List<Operation> operations = List.of(createOperationWithCurrencyMetadata());

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromOperations(operations);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).contains(
                AssetFingerprint.of("policy1", "token1")
            );
        }

        @Test
        @DisplayName("Should exclude lovelace from extraction")
        void shouldExcludeLovelaceFromExtractionInOperations() {
            // given
            List<Operation> operations = List.of(createOperationWithLovelaceAndTokens());

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromOperations(operations);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).contains(
                AssetFingerprint.of("policy1", "token1")
            );
        }

        @Test
        @DisplayName("Should handle operations with null metadata")
        void shouldHandleOperationsWithNullMetadata() {
            // given
            Operation operation = Operation.builder()
                .metadata(null)
                .amount(null)
                .build();
            List<Operation> operations = List.of(operation);

            // when
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromOperations(operations);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Helper Method Tests - fetchMetadataFor* variants")
    class FetchMetadataHelperMethodTests {


        @Test
        @DisplayName("fetchMetadataForBlockTx should call gateway for BlockTx with assets")
        void fetchMetadataForBlockTxShouldCallGatewayWithAssets() {
            // given
            BlockTx blockTx = createBlockTxWithInputs();
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of());

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.fetchMetadataForBlockTx(blockTx);

            // then
            assertThat(result).hasSize(2);
            verify(tokenRegistryHttpGateway).getTokenMetadataBatch(anySet());
        }


        @Test
        @DisplayName("fetchMetadataForBlockTransactions should return empty map for empty transactions")
        void fetchMetadataForBlockTransactionsShouldReturnEmptyMapForEmpty() {
            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.fetchMetadataForBlockTransactions(List.of());

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(tokenRegistryHttpGateway);
        }

        @Test
        @DisplayName("fetchMetadataForBlockTxList should return empty map for null list")
        void fetchMetadataForBlockTxListShouldReturnEmptyMapForNull() {
            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.fetchMetadataForBlockTxList(null);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(tokenRegistryHttpGateway);
        }

        @Test
        @DisplayName("fetchMetadataForBlockTxList should return empty map for empty list")
        void fetchMetadataForBlockTxListShouldReturnEmptyMapForEmpty() {
            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.fetchMetadataForBlockTxList(List.of());

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(tokenRegistryHttpGateway);
        }

        @Test
        @DisplayName("fetchMetadataForBlockTxList should aggregate assets from multiple transactions")
        void fetchMetadataForBlockTxListShouldAggregateAssetsFromMultipleTx() {
            // given
            List<BlockTx> blockTxList = List.of(
                createBlockTxWithInputs(),
                createBlockTxWithOutputs()
            );
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of());

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.fetchMetadataForBlockTxList(blockTxList);

            // then
            assertThat(result).hasSize(4); // 2 from inputs + 2 from outputs
            verify(tokenRegistryHttpGateway).getTokenMetadataBatch(anySet());
        }


        @Test
        @DisplayName("fetchMetadataForAddressBalances should return empty map for empty balances")
        void fetchMetadataForAddressBalancesShouldReturnEmptyMapForEmpty() {
            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.fetchMetadataForAddressBalances(List.of());

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(tokenRegistryHttpGateway);
        }

        @Test
        @DisplayName("fetchMetadataForAddressBalances should extract assets from valid balances")
        void fetchMetadataForAddressBalancesShouldExtractAssetsFromValidBalances() {
            // given
            List<AddressBalance> balances = List.of(
                createAddressBalance(LOVELACE), // Should be excluded
                createAddressBalance("a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3token1"),
                createAddressBalance("short") // Should be excluded - too short
            );
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of());

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.fetchMetadataForAddressBalances(balances);

            // then
            assertThat(result).hasSize(1);
            verify(tokenRegistryHttpGateway).getTokenMetadataBatch(anySet());
        }


        @Test
        @DisplayName("fetchMetadataForUtxos should return empty map for empty utxos")
        void fetchMetadataForUtxosShouldReturnEmptyMapForEmpty() {
            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.fetchMetadataForUtxos(List.of());

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(tokenRegistryHttpGateway);
        }

        @Test
        @DisplayName("fetchMetadataForUtxos should extract assets from utxos with native tokens")
        void fetchMetadataForUtxosShouldExtractAssetsFromUtxosWithNativeTokens() {
            // given
            List<Utxo> utxos = List.of(
                createUtxoWithAmounts(List.of(
                    createAmt(LOVELACE, null, LOVELACE), // Should be excluded
                    createAmt("token1", "policy1", "token1"),
                    createAmt("token2", "policy2", "token2")
                ))
            );
            when(tokenRegistryHttpGateway.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of());

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.fetchMetadataForUtxos(utxos);

            // then
            assertThat(result).hasSize(2);
            verify(tokenRegistryHttpGateway).getTokenMetadataBatch(anySet());
        }


        @Test
        @DisplayName("fetchMetadataForUtxos should handle amounts with null policyId")
        void fetchMetadataForUtxosShouldHandleAmountsWithNullPolicyId() {
            // given
            List<Utxo> utxos = List.of(
                createUtxoWithAmounts(List.of(
                    createAmt("token1", null, "token1") // null policyId should be excluded
                ))
            );

            // when
            Map<AssetFingerprint, TokenRegistryCurrencyData> result = tokenRegistryService.fetchMetadataForUtxos(utxos);

            // then
            assertThat(result).isEmpty();
            verifyNoInteractions(tokenRegistryHttpGateway);
        }
    }

    // Helper methods
    private AssetFingerprint createAsset(String policyId, String symbolHex) {
        return AssetFingerprint.of(policyId, symbolHex);
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

    // Additional helper methods for new tests
    private BlockTx createBlockTxWithInputs() {
        return BlockTx.builder()
            .inputs(List.of(
                createUtxoWithAmounts(List.of(
                    createAmt("token1", "policy1", "token1"),
                    createAmt(LOVELACE, null, LOVELACE)
                )),
                createUtxoWithAmounts(List.of(
                    createAmt("token2", "policy2", "token2")
                ))
            ))
            .build();
    }

    private BlockTx createBlockTxWithOutputs() {
        return BlockTx.builder()
            .outputs(List.of(
                createUtxoWithAmounts(List.of(
                    createAmt("token3", "policy3", "token3"),
                    createAmt(LOVELACE, null, LOVELACE)
                )),
                createUtxoWithAmounts(List.of(
                    createAmt("token4", "policy4", "token4")
                ))
            ))
            .build();
    }

    private BlockTx createBlockTxWithInputsAndOutputs() {
        return BlockTx.builder()
            .inputs(List.of(
                createUtxoWithAmounts(List.of(
                    createAmt("token1", "policy1", "token1")
                )),
                createUtxoWithAmounts(List.of(
                    createAmt("token2", "policy2", "token2")
                ))
            ))
            .outputs(List.of(
                createUtxoWithAmounts(List.of(
                    createAmt("token3", "policy3", "token3")
                )),
                createUtxoWithAmounts(List.of(
                    createAmt("token4", "policy4", "token4")
                ))
            ))
            .build();
    }

    private BlockTx createBlockTxWithLovelaceAndTokens() {
        return BlockTx.builder()
            .inputs(List.of(
                createUtxoWithAmounts(List.of(
                    createAmt(LOVELACE, null, LOVELACE),
                    createAmt("token1", "policy1", "token1")
                ))
            ))
            .build();
    }

    private Utxo createUtxoWithAmounts(List<Amt> amounts) {
        return Utxo.builder().amounts(amounts).build();
    }

    private Amt createAmt(String assetName, String policyId, String unit) {
        return Amt.builder()
            .assetName(assetName)
            .policyId(policyId)
            .unit(unit)
            .quantity(BigDecimal.valueOf(1000000).toBigInteger())
            .build();
    }

    private BlockTransaction createBlockTransactionWithTokenBundles() {
        List<Amount> tokens1 = List.of(
            Amount.builder()
                .currency(CurrencyResponse.builder().symbol("token1").build())
                .value("1000")
                .build()
        );

        List<Amount> tokens2 = List.of(
            Amount.builder()
                .currency(CurrencyResponse.builder().symbol("token2").build())
                .value("2000")
                .build()
        );

        TokenBundleItem bundle1 = TokenBundleItem.builder()
            .policyId("policy1")
            .tokens(tokens1)
            .build();

        TokenBundleItem bundle2 = TokenBundleItem.builder()
            .policyId("policy2")
            .tokens(tokens2)
            .build();

        OperationMetadata metadata = OperationMetadata.builder()
            .tokenBundle(List.of(bundle1, bundle2))
            .build();

        Operation operation = Operation.builder()
            .metadata(metadata)
            .build();

        Transaction transaction = Transaction.builder()
            .operations(List.of(operation))
            .build();

        return BlockTransaction.builder()
            .transaction(transaction)
            .build();
    }

    private BlockTransaction createBlockTransactionWithCurrencyMetadata() {
        CurrencyMetadataResponse metadata = CurrencyMetadataResponse.builder()
            .policyId("policy1")
            .build();

        CurrencyResponse currency = CurrencyResponse.builder()
            .symbol("token1")
            .metadata(metadata)
            .build();

        Amount amount = Amount.builder()
            .currency(currency)
            .value("1000")
            .build();

        Operation operation = Operation.builder()
            .amount(amount)
            .build();

        Transaction transaction = Transaction.builder()
            .operations(List.of(operation))
            .build();

        return BlockTransaction.builder()
            .transaction(transaction)
            .build();
    }

    private Operation createOperationWithTokenBundle() {
        List<Amount> tokens = List.of(
            Amount.builder()
                .currency(CurrencyResponse.builder().symbol("token1").build())
                .value("1000")
                .build(),
            Amount.builder()
                .currency(CurrencyResponse.builder().symbol("token2").build())
                .value("2000")
                .build()
        );

        TokenBundleItem bundle = TokenBundleItem.builder()
            .policyId("policy1")
            .tokens(tokens)
            .build();

        OperationMetadata metadata = OperationMetadata.builder()
            .tokenBundle(List.of(bundle))
            .build();

        return Operation.builder()
            .metadata(metadata)
            .build();
    }

    private Operation createOperationWithCurrencyMetadata() {
        CurrencyMetadataResponse metadata = CurrencyMetadataResponse.builder()
            .policyId("policy1")
            .build();

        CurrencyResponse currency = CurrencyResponse.builder()
            .symbol("token1")
            .metadata(metadata)
            .build();

        Amount amount = Amount.builder()
            .currency(currency)
            .value("1000")
            .build();

        return Operation.builder()
            .amount(amount)
            .build();
    }

    private Operation createOperationWithLovelaceAndTokens() {
        List<Amount> tokens = List.of(
            Amount.builder()
                .currency(CurrencyResponse.builder().symbol(LOVELACE).build())
                .value("1000000")
                .build(),
            Amount.builder()
                .currency(CurrencyResponse.builder().symbol("token1").build())
                .value("1000")
                .build()
        );

        TokenBundleItem bundle = TokenBundleItem.builder()
            .policyId("policy1")
            .tokens(tokens)
            .build();

        OperationMetadata metadata = OperationMetadata.builder()
            .tokenBundle(List.of(bundle))
            .build();

        return Operation.builder()
            .metadata(metadata)
            .build();
    }

    private AddressBalance createAddressBalance(String unit) {
        return AddressBalance.builder()
            .unit(unit)
            .quantity(BigDecimal.valueOf(1000000).toBigInteger())
            .build();
    }
}