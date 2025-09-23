package org.cardanofoundation.rosetta.api.account.mapper;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.common.model.Asset;
import org.cardanofoundation.rosetta.api.common.service.TokenRegistryService;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Coin;
import org.openapitools.client.model.CoinTokens;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.LogoType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountMapperUtilTest {

    @Mock
    private TokenRegistryService tokenRegistryService;

    private AccountMapperUtil accountMapperUtil;

    @BeforeEach
    void setUp() {
        accountMapperUtil = new AccountMapperUtil();

        // Configure TokenRegistryService to return fallback metadata for any asset
        lenient().when(tokenRegistryService.getTokenMetadataBatch(anySet())).thenAnswer(invocation -> {
            java.util.Map<Asset, CurrencyMetadataResponse> result = new java.util.HashMap<>();
            @SuppressWarnings("unchecked")
            java.util.Set<Asset> assets = (java.util.Set<Asset>) invocation.getArgument(0);
            for (Asset asset : assets) {
                result.put(asset, CurrencyMetadataResponse.builder()
                    .policyId(asset.getPolicyId())
                    .decimals(0) // Default decimals
                    .build());
            }
            return result;
        });
    }

    // Helper method to create metadata map from balances
    private Map<Asset, CurrencyMetadataResponse> createMetadataMapFromBalances(List<AddressBalance> balances) {
        Set<Asset> assets = balances.stream()
            .filter(b -> !Constants.LOVELACE.equals(b.unit()))
            .filter(b -> b.unit().length() >= Constants.POLICY_ID_LENGTH)
            .map(b -> {
                String symbol = b.unit().substring(Constants.POLICY_ID_LENGTH);
                String policyId = b.unit().substring(0, Constants.POLICY_ID_LENGTH);
                return Asset.builder()
                    .policyId(policyId)
                    .assetName(symbol)
                    .build();
            })
            .collect(Collectors.toSet());

        if (assets.isEmpty()) {
            return Collections.emptyMap();
        }

        return tokenRegistryService.getTokenMetadataBatch(assets);
    }

    // Helper method to create metadata map from UTXOs
    private Map<Asset, CurrencyMetadataResponse> createMetadataMapFromUtxos(List<Utxo> utxos) {
        Set<Asset> assets = new HashSet<>();
        for (Utxo utxo : utxos) {
            if (utxo.getAmounts() != null) {
                for (Amt amount : utxo.getAmounts()) {
                    if (!Constants.LOVELACE.equals(amount.getAssetName()) && amount.getPolicyId() != null) {
                        assets.add(Asset.builder()
                            .policyId(amount.getPolicyId())
                            .assetName(amount.getAssetName())
                            .build());
                    }
                }
            }
        }

        if (assets.isEmpty()) {
            return Collections.emptyMap();
        }

        return tokenRegistryService.getTokenMetadataBatch(assets);
    }

    @Nested
    class MapAddressBalancesToAmountsTests {

        @Test
        void shouldMapOnlyLovelaceBalance() {
            // given
            List<AddressBalance> balances = List.of(
                    createAddressBalance(Constants.LOVELACE, BigInteger.valueOf(1000000))
            );

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromBalances(balances);
            List<Amount> amounts = accountMapperUtil.mapAddressBalancesToAmounts(balances, metadataMap);

            // then
            assertEquals(1, amounts.size());
            Amount adaAmount = amounts.get(0);
            assertEquals("1000000", adaAmount.getValue());
            assertEquals(Constants.ADA, adaAmount.getCurrency().getSymbol());
            assertEquals(Constants.ADA_DECIMALS, adaAmount.getCurrency().getDecimals());
            assertNull(adaAmount.getCurrency().getMetadata());
        }

        @Test
        void shouldMapLovelaceAndNativeTokensWithoutRegistry() {
            // given
            String policyId = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3"; // exactly 56 chars
            String assetName = "TestToken";
            String unit = policyId + assetName;
            
            // Mock service to return fallback metadata (service always returns something now)
            Asset asset = Asset.builder().policyId(policyId).assetName(assetName).build();
            CurrencyMetadataResponse fallbackMetadata = CurrencyMetadataResponse.builder()
                .policyId(policyId)
                .build();
            Map<Asset, CurrencyMetadataResponse> tokenMetadataMap = Map.of(asset, fallbackMetadata);
            when(tokenRegistryService.getTokenMetadataBatch(anySet())).thenReturn(tokenMetadataMap);
            
            List<AddressBalance> balances = List.of(
                    createAddressBalance(Constants.LOVELACE, BigInteger.valueOf(2000000)),
                    createAddressBalance(unit, BigInteger.valueOf(500))
            );

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromBalances(balances);
            List<Amount> amounts = accountMapperUtil.mapAddressBalancesToAmounts(balances, metadataMap);

            // then
            assertEquals(2, amounts.size());
            
            // ADA amount
            Amount adaAmount = amounts.get(0);
            assertEquals("2000000", adaAmount.getValue());
            assertEquals(Constants.ADA, adaAmount.getCurrency().getSymbol());
            
            // Native token amount - symbol should be just the asset name part
            Amount tokenAmount = amounts.get(1);
            assertEquals("500", tokenAmount.getValue());
            assertEquals(assetName, tokenAmount.getCurrency().getSymbol());
            assertEquals(Constants.MULTI_ASSET_DECIMALS, tokenAmount.getCurrency().getDecimals());
            
            // Verify metadata contains only policyId (no registry data)
            CurrencyMetadataResponse metadata = tokenAmount.getCurrency().getMetadata();
            assertNotNull(metadata);
            assertEquals(policyId, metadata.getPolicyId());
            assertNull(metadata.getName());
            assertNull(metadata.getDescription());
        }

        @Test
        void shouldMapNativeTokensWithRegistryData() {
            // given
            String policyId = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3"; // exactly 56 chars
            String assetName = "TestToken";
            String unit = policyId + assetName;
            String subject = policyId + "54657374546f6b656e"; // hex encoding of "TestToken"
            
            // Mock token registry response
            CurrencyMetadataResponse currencyMetadata = createCurrencyMetadata(policyId, subject, "Test Token", "Test description", 
                    "TST", "https://test.com", "logo", 6, 1L);
            
            Map<Asset, CurrencyMetadataResponse> tokenMetadataMap = new HashMap<>();
            Asset asset = Asset.builder().policyId(policyId).assetName(assetName).build();
            tokenMetadataMap.put(asset, currencyMetadata);
            
            when(tokenRegistryService.getTokenMetadataBatch(anySet())).thenReturn(tokenMetadataMap);
            
            List<AddressBalance> balances = List.of(
                    createAddressBalance(Constants.LOVELACE, BigInteger.valueOf(1500000)),
                    createAddressBalance(unit, BigInteger.valueOf(750))
            );

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromBalances(balances);
            List<Amount> amounts = accountMapperUtil.mapAddressBalancesToAmounts(balances, metadataMap);

            // then
            assertEquals(2, amounts.size());
            
            // Native token amount with registry data
            Amount tokenAmount = amounts.get(1);
            assertEquals("750", tokenAmount.getValue());
            assertEquals(assetName, tokenAmount.getCurrency().getSymbol());
            assertEquals(6, tokenAmount.getCurrency().getDecimals()); // From registry
            
            // Verify full metadata from registry
            CurrencyMetadataResponse metadata = tokenAmount.getCurrency().getMetadata();
            assertNotNull(metadata);
            assertEquals(policyId, metadata.getPolicyId());
            assertEquals(subject, metadata.getSubject());
            assertEquals("Test Token", metadata.getName());
            assertEquals("Test description", metadata.getDescription());
            assertEquals("TST", metadata.getTicker());
            assertEquals("https://test.com", metadata.getUrl());
            assertNotNull(metadata.getLogo());
            assertEquals("logo", metadata.getLogo().getValue());
            assertEquals(BigDecimal.valueOf(1L), metadata.getVersion());
        }

        @Test
        void shouldHandleMultipleNativeTokensWithBatchRegistryCall() {
            // given
            String policyId1 = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3"; // exactly 56 chars
            String policyId2 = "f5e4d3c2b1a0f5e4d3c2b1a0f5e4d3c2b1a0f5e4d3c2b1a0f5e4d3c2"; // exactly 56 chars
            String assetName1 = "Token1";
            String assetName2 = "Token2";
            String unit1 = policyId1 + assetName1;
            String unit2 = policyId2 + assetName2;
            String subject1 = policyId1 + "546f6b656e31"; // hex of "Token1"
            String subject2 = policyId2 + "546f6b656e32"; // hex of "Token2"
            
            // Mock batch registry response
            Map<Asset, CurrencyMetadataResponse> tokenMetadataMap = new HashMap<>();
            Asset asset1 = Asset.builder().policyId(policyId1).assetName(assetName1).build();
            Asset asset2 = Asset.builder().policyId(policyId2).assetName(assetName2).build();
            tokenMetadataMap.put(asset1, createCurrencyMetadata(policyId1, subject1, "First Token", "First desc", "TK1", null, null, 8, null));
            tokenMetadataMap.put(asset2, CurrencyMetadataResponse.builder().policyId(policyId2).build()); // Fallback metadata for second token
            
            when(tokenRegistryService.getTokenMetadataBatch(anySet())).thenReturn(tokenMetadataMap);
            
            List<AddressBalance> balances = List.of(
                    createAddressBalance(Constants.LOVELACE, BigInteger.valueOf(3000000)),
                    createAddressBalance(unit1, BigInteger.valueOf(100)),
                    createAddressBalance(unit2, BigInteger.valueOf(200))
            );

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromBalances(balances);
            List<Amount> amounts = accountMapperUtil.mapAddressBalancesToAmounts(balances, metadataMap);

            // then
            assertEquals(3, amounts.size());
            
            // First token with registry data
            Amount token1Amount = amounts.get(1);
            assertEquals("100", token1Amount.getValue());
            assertEquals(8, token1Amount.getCurrency().getDecimals());
            assertEquals("First Token", token1Amount.getCurrency().getMetadata().getName());
            
            // Second token without registry data
            Amount token2Amount = amounts.get(2);
            assertEquals("200", token2Amount.getValue());
            assertEquals(Constants.MULTI_ASSET_DECIMALS, token2Amount.getCurrency().getDecimals());
            assertEquals(policyId2, token2Amount.getCurrency().getMetadata().getPolicyId());
            assertNull(token2Amount.getCurrency().getMetadata().getName());
        }

        @Test
        void shouldHandleEmptyBalances() {
            // given
            List<AddressBalance> balances = Collections.emptyList();

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromBalances(balances);
            List<Amount> amounts = accountMapperUtil.mapAddressBalancesToAmounts(balances, metadataMap);

            // then
            assertEquals(1, amounts.size()); // Only ADA with 0 amount
            Amount adaAmount = amounts.get(0);
            assertEquals("0", adaAmount.getValue());
            assertEquals(Constants.ADA, adaAmount.getCurrency().getSymbol());
        }

        @Test
        void shouldHandleTokenWithNullMetadataFields() {
            // Test case for token with null name and description - should be treated as not found
            String policyId = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3"; // exactly 56 chars
            String assetName = "TestToken";
            String unit = policyId + assetName;
            String subject = policyId + "54657374546f6b656e"; // hex of "TestToken"
            
            List<AddressBalance> balances = List.of(
                createAddressBalance(Constants.LOVELACE, BigInteger.valueOf(1000000)),
                createAddressBalance(unit, BigInteger.valueOf(500))
            );

            // Token with null metadata fields should return fallback metadata with only policyId
            Asset asset = Asset.builder().policyId(policyId).assetName(assetName).build();
            when(tokenRegistryService.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(asset, CurrencyMetadataResponse.builder().policyId(policyId).build()));

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromBalances(balances);
            List<Amount> amounts = accountMapperUtil.mapAddressBalancesToAmounts(balances, metadataMap);

            // then
            assertEquals(2, amounts.size());
            
            // Token should not have enriched metadata
            Amount tokenAmount = amounts.get(1);
            assertEquals("500", tokenAmount.getValue());
            assertEquals(assetName, tokenAmount.getCurrency().getSymbol());
            assertEquals(Constants.MULTI_ASSET_DECIMALS, tokenAmount.getCurrency().getDecimals());
            
            CurrencyMetadataResponse metadata = tokenAmount.getCurrency().getMetadata();
            assertNotNull(metadata);
            assertEquals(policyId, metadata.getPolicyId());
            // Should not have enriched fields when token is not in registry
            assertNull(metadata.getName());
            assertNull(metadata.getDescription());
            assertNull(metadata.getSubject());
        }

        @Test
        void shouldHandleTokenWithNullMetadataObject() {
            // Test case for token with completely null metadata - should be treated as not found
            String policyId = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3"; // exactly 56 chars
            String assetName = "TestToken";
            String unit = policyId + assetName;
            String subject = policyId + "54657374546f6b656e"; // hex of "TestToken"
            
            List<AddressBalance> balances = List.of(
                createAddressBalance(Constants.LOVELACE, BigInteger.valueOf(1000000)),
                createAddressBalance(unit, BigInteger.valueOf(500))
            );

            // Token with null metadata should return fallback metadata with only policyId
            Asset asset = Asset.builder().policyId(policyId).assetName(assetName).build();
            when(tokenRegistryService.getTokenMetadataBatch(anySet()))
                .thenReturn(Map.of(asset, CurrencyMetadataResponse.builder().policyId(policyId).build()));

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromBalances(balances);
            List<Amount> amounts = accountMapperUtil.mapAddressBalancesToAmounts(balances, metadataMap);

            // then
            assertEquals(2, amounts.size());
            
            Amount tokenAmount = amounts.get(1);
            assertEquals("500", tokenAmount.getValue());
            assertEquals(assetName, tokenAmount.getCurrency().getSymbol());
            assertEquals(Constants.MULTI_ASSET_DECIMALS, tokenAmount.getCurrency().getDecimals());
            
            CurrencyMetadataResponse metadata = tokenAmount.getCurrency().getMetadata();
            assertNotNull(metadata);
            assertEquals(policyId, metadata.getPolicyId());
            // Should not have enriched fields when token is not in registry
            assertNull(metadata.getName());
            assertNull(metadata.getDescription());
            assertNull(metadata.getSubject());
        }
    }

    @Nested
    class MapUtxosToCoinsTests {

        @Test
        void shouldMapUtxosWithOnlyAda() {
            // given
            List<Utxo> utxos = List.of(
                    createUtxo("txhash1", 0, List.of(
                            createAmt(null, Constants.LOVELACE, BigInteger.valueOf(1000000))
                    ))
            );

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromUtxos(utxos);
            List<Coin> coins = accountMapperUtil.mapUtxosToCoins(utxos, metadataMap);

            // then
            assertEquals(1, coins.size());
            Coin coin = coins.get(0);
            assertEquals("txhash1:0", coin.getCoinIdentifier().getIdentifier());
            assertEquals("1000000", coin.getAmount().getValue());
            assertEquals(Constants.ADA, coin.getAmount().getCurrency().getSymbol());
            assertNull(coin.getMetadata()); // No native tokens
        }

        @Test
        void shouldMapUtxosWithNativeTokensAndRegistry() {
            // given
            String policyId = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3"; // exactly 56 chars
            String assetName = "TestToken";
            String unit = policyId + assetName;
            String subject = policyId + "54657374546f6b656e"; // hex of "TestToken"
            
            // Mock registry response
            CurrencyMetadataResponse currencyMetadata = createCurrencyMetadata(policyId, subject, "Test Token", "Test desc", "TST", null, null, 4, null);
            Asset asset = Asset.builder().policyId(policyId).assetName(assetName).build();
            Map<Asset, CurrencyMetadataResponse> tokenMetadataMap = Map.of(asset, currencyMetadata);
            when(tokenRegistryService.getTokenMetadataBatch(anySet())).thenReturn(tokenMetadataMap);
            
            List<Utxo> utxos = List.of(
                    createUtxo("txhash1", 0, List.of(
                            createAmt(null, Constants.LOVELACE, BigInteger.valueOf(2000000)),
                            createAmt(policyId, assetName, BigInteger.valueOf(500), unit)
                    ))
            );

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromUtxos(utxos);
            List<Coin> coins = accountMapperUtil.mapUtxosToCoins(utxos, metadataMap);

            // then
            assertEquals(1, coins.size());
            Coin coin = coins.get(0);
            assertEquals("txhash1:0", coin.getCoinIdentifier().getIdentifier());
            assertEquals("2000000", coin.getAmount().getValue()); // ADA amount
            
            // Verify native token metadata
            assertNotNull(coin.getMetadata());
            List<CoinTokens> coinTokens = coin.getMetadata().get("txhash1:0");
            assertEquals(1, coinTokens.size());
            
            CoinTokens tokens = coinTokens.get(0);
            assertEquals(policyId, tokens.getPolicyId());
            assertEquals(1, tokens.getTokens().size());
            
            Amount tokenAmount = tokens.getTokens().get(0);
            assertEquals("500", tokenAmount.getValue());
            assertEquals(assetName, tokenAmount.getCurrency().getSymbol());
            assertEquals(4, tokenAmount.getCurrency().getDecimals()); // From registry
            assertEquals("Test Token", tokenAmount.getCurrency().getMetadata().getName());
        }

        @Test
        void shouldMapUtxosWithMultipleNativeTokensSamePolicyId() {
            // given
            String policyId = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3"; // exactly 56 chars
            String assetName1 = "Token1";
            String assetName2 = "Token2";
            String unit1 = policyId + assetName1;
            String unit2 = policyId + assetName2;
            
            List<Utxo> utxos = List.of(
                    createUtxo("txhash1", 0, List.of(
                            createAmt(null, Constants.LOVELACE, BigInteger.valueOf(1500000)),
                            createAmt(policyId, assetName1, BigInteger.valueOf(100), unit1),
                            createAmt(policyId, assetName2, BigInteger.valueOf(200), unit2)
                    ))
            );

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromUtxos(utxos);
            List<Coin> coins = accountMapperUtil.mapUtxosToCoins(utxos, metadataMap);

            // then
            assertEquals(1, coins.size());
            Coin coin = coins.get(0);
            
            // Should have two separate CoinTokens entries (one per token)
            List<CoinTokens> coinTokens = coin.getMetadata().get("txhash1:0");
            assertEquals(2, coinTokens.size());
            
            // Both tokens have the same policy ID but are in separate CoinTokens entries
            assertEquals(policyId, coinTokens.get(0).getPolicyId());
            assertEquals(policyId, coinTokens.get(1).getPolicyId());
            assertEquals(1, coinTokens.get(0).getTokens().size());
            assertEquals(1, coinTokens.get(1).getTokens().size());
        }

        @Test
        void shouldHandleEmptyUtxos() {
            // given
            List<Utxo> utxos = Collections.emptyList();

            // when
            Map<Asset, CurrencyMetadataResponse> metadataMap = createMetadataMapFromUtxos(utxos);
            List<Coin> coins = accountMapperUtil.mapUtxosToCoins(utxos, metadataMap);

            // then
            assertTrue(coins.isEmpty());
        }
    }

    // Helper methods
    private AddressBalance createAddressBalance(String unit, BigInteger quantity) {
        return AddressBalance.builder()
                .address("addr_test123")
                .unit(unit)
                .slot(1000L)
                .quantity(quantity)
                .number(100L)
                .build();
    }

    private Utxo createUtxo(String txHash, int outputIndex, List<Amt> amounts) {
        return Utxo.builder()
                .txHash(txHash)
                .outputIndex(outputIndex)
                .amounts(amounts)
                .build();
    }

    private Amt createAmt(String policyId, String assetName, BigInteger quantity) {
        return createAmt(policyId, assetName, quantity, null);
    }

    private Amt createAmt(String policyId, String assetName, BigInteger quantity, String unit) {
        return Amt.builder()
                .policyId(policyId)
                .assetName(assetName)
                .quantity(quantity)
                .unit(unit != null ? unit : (policyId != null ? policyId + assetName : assetName))
                .build();
    }

    private CurrencyMetadataResponse createCurrencyMetadata(String policyId, String subject, String name, String description, 
                                          String ticker, String url, String logo, Integer decimals, Long version) {
        CurrencyMetadataResponse.CurrencyMetadataResponseBuilder builder = CurrencyMetadataResponse.builder()
                .policyId(policyId)
                .subject(subject)
                .name(name)
                .description(description);
        
        if (ticker != null) {
            builder.ticker(ticker);
        }
        if (url != null) {
            builder.url(url);
        }
        if (logo != null) {
            builder.logo(LogoType.builder().format(LogoType.FormatEnum.BASE64).value(logo).build());
        }
        if (decimals != null) {
            builder.decimals(decimals);
        }
        if (version != null) {
            builder.version(BigDecimal.valueOf(version));
        }
        
        return builder.build();
    }
}