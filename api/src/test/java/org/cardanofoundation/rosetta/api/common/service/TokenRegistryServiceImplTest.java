package org.cardanofoundation.rosetta.api.common.service;

import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.Cip26StorageReader;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.impl.model.TokenMetadata;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.model.FungibleTokenMetadata;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.storage.Cip68StorageReader;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenRegistryServiceImpl Tests")
class TokenRegistryServiceImplTest {

    @Mock
    private Cip26StorageReader cip26StorageReader;

    @Mock
    private Cip68StorageReader cip68StorageReader;

    private TokenRegistryServiceImpl tokenRegistryService;

    private static final String POLICY_ID = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3";
    private static final String ASSET_SYMBOL_HEX = "54657374546f6b656e";
    private static final String SUBJECT = POLICY_ID + ASSET_SYMBOL_HEX;

    @BeforeEach
    void setUp() {
        tokenRegistryService = new TokenRegistryServiceImpl(cip26StorageReader, cip68StorageReader);
    }

    @Nested
    @DisplayName("getTokenMetadataBatch Tests")
    class GetTokenMetadataBatchTests {

        @Test
        @DisplayName("Should return empty map when assets set is empty")
        void shouldReturnEmptyMapForEmptyAssets() {
            Map<AssetFingerprint, TokenRegistryCurrencyData> result =
                    tokenRegistryService.getTokenMetadataBatch(Set.of());

            assertThat(result).isEmpty();
            verifyNoInteractions(cip26StorageReader, cip68StorageReader);
        }

        @Test
        @DisplayName("Should return metadata from CIP-26 when available")
        void shouldReturnCip26Metadata() {
            AssetFingerprint asset = AssetFingerprint.of(POLICY_ID, ASSET_SYMBOL_HEX);

            TokenMetadata cip26 = createCip26Metadata(SUBJECT, "Test Token", "A test token", "TST", 6L);
            when(cip26StorageReader.findBySubjects(anyList())).thenReturn(List.of(cip26));
            when(cip68StorageReader.findBySubject(anyString())).thenReturn(Optional.empty());

            Map<AssetFingerprint, TokenRegistryCurrencyData> result =
                    tokenRegistryService.getTokenMetadataBatch(Set.of(asset));

            assertThat(result).hasSize(1);
            TokenRegistryCurrencyData data = result.get(asset);
            assertThat(data.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(data.getSubject()).isEqualTo(SUBJECT);
            assertThat(data.getName()).isEqualTo("Test Token");
            assertThat(data.getDescription()).isEqualTo("A test token");
            assertThat(data.getTicker()).isEqualTo("TST");
            assertThat(data.getDecimals()).isEqualTo(6);
        }

        @Test
        @DisplayName("CIP-68 should override CIP-26 when both available")
        void cip68ShouldOverrideCip26() {
            AssetFingerprint asset = AssetFingerprint.of(POLICY_ID, ASSET_SYMBOL_HEX);

            TokenMetadata cip26 = createCip26Metadata(SUBJECT, "CIP26 Name", "CIP26 Desc", "C26", 0L);
            FungibleTokenMetadata cip68 = new FungibleTokenMetadata(8L, "CIP68 Desc", null, "CIP68 Name", "C68", null, 1L);

            when(cip26StorageReader.findBySubjects(anyList())).thenReturn(List.of(cip26));
            when(cip68StorageReader.findBySubject(SUBJECT)).thenReturn(Optional.of(cip68));

            Map<AssetFingerprint, TokenRegistryCurrencyData> result =
                    tokenRegistryService.getTokenMetadataBatch(Set.of(asset));

            TokenRegistryCurrencyData data = result.get(asset);
            assertThat(data.getName()).isEqualTo("CIP68 Name");
            assertThat(data.getDescription()).isEqualTo("CIP68 Desc");
            assertThat(data.getTicker()).isEqualTo("C68");
            assertThat(data.getDecimals()).isEqualTo(8);
            assertThat(data.getVersion()).isEqualTo(BigDecimal.valueOf(1L));
        }

        @Test
        @DisplayName("Should return fallback when neither CIP-26 nor CIP-68 has data")
        void shouldReturnFallbackWhenNoData() {
            AssetFingerprint asset = AssetFingerprint.of(POLICY_ID, ASSET_SYMBOL_HEX);

            when(cip26StorageReader.findBySubjects(anyList())).thenReturn(List.of());
            when(cip68StorageReader.findBySubject(anyString())).thenReturn(Optional.empty());

            Map<AssetFingerprint, TokenRegistryCurrencyData> result =
                    tokenRegistryService.getTokenMetadataBatch(Set.of(asset));

            assertThat(result).hasSize(1);
            TokenRegistryCurrencyData data = result.get(asset);
            assertThat(data.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(data.getName()).isNull();
        }

        @Test
        @DisplayName("Should handle multiple assets correctly")
        void shouldHandleMultipleAssets() {
            AssetFingerprint asset1 = AssetFingerprint.of(POLICY_ID, ASSET_SYMBOL_HEX);
            AssetFingerprint asset2 = AssetFingerprint.of("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "aabb");

            TokenMetadata cip26 = createCip26Metadata(SUBJECT, "Token1", "Desc1", null, null);
            when(cip26StorageReader.findBySubjects(anyList())).thenReturn(List.of(cip26));
            when(cip68StorageReader.findBySubject(anyString())).thenReturn(Optional.empty());

            Map<AssetFingerprint, TokenRegistryCurrencyData> result =
                    tokenRegistryService.getTokenMetadataBatch(Set.of(asset1, asset2));

            assertThat(result).hasSize(2);
            assertThat(result.get(asset1).getName()).isEqualTo("Token1");
            assertThat(result.get(asset2).getName()).isNull();
        }
    }

    @Nested
    @DisplayName("Asset Extraction from BlockTx Tests")
    class AssetExtractionFromBlockTxTests {

        @Test
        @DisplayName("Should extract assets from inputs and outputs")
        void shouldExtractAssetsFromInputsAndOutputs() {
            BlockTx blockTx = createBlockTxWithInputsAndOutputs();
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);
            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("Should exclude lovelace from extraction")
        void shouldExcludeLovelace() {
            BlockTx blockTx = createBlockTxWithLovelaceAndTokens();
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);
            assertThat(result).hasSize(1);
            assertThat(result).contains(AssetFingerprint.of("policy1", "token1"));
        }

        @Test
        @DisplayName("Should handle empty inputs and outputs")
        void shouldHandleEmpty() {
            BlockTx blockTx = BlockTx.builder().inputs(List.of()).outputs(List.of()).build();
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle null amounts in utxos")
        void shouldHandleNullAmounts() {
            Utxo utxo = Utxo.builder().amounts(null).build();
            BlockTx blockTx = BlockTx.builder().inputs(List.of(utxo)).build();
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Asset Extraction from Amounts Tests")
    class AssetExtractionFromAmountsTests {

        @Test
        @DisplayName("Should return empty set when amounts is empty")
        void shouldReturnEmptyForEmpty() {
            assertThat(tokenRegistryService.extractAssetsFromAmounts(List.of())).isEmpty();
        }

        @Test
        @DisplayName("Should extract native tokens only")
        void shouldExtractNativeTokensOnly() {
            List<Amt> amounts = List.of(
                    createAmt(LOVELACE, null, LOVELACE),
                    createAmt("token1", "policy1", "token1"),
                    createAmt("token2", "policy2", "token2")
            );
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromAmounts(amounts);
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Asset Extraction from BlockTransactions Tests")
    class AssetExtractionFromBlockTransactionsTests {

        @Test
        @DisplayName("Should return empty set when empty")
        void shouldReturnEmptyForEmpty() {
            assertThat(tokenRegistryService.extractAssetsFromBlockTransactions(List.of())).isEmpty();
        }

        @Test
        @DisplayName("Should extract assets from token bundles")
        void shouldExtractFromTokenBundles() {
            List<BlockTransaction> txs = List.of(createBlockTransactionWithTokenBundles());
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTransactions(txs);
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Asset Extraction from Operations Tests")
    class AssetExtractionFromOperationsTests {

        @Test
        @DisplayName("Should return empty set when empty")
        void shouldReturnEmptyForEmpty() {
            assertThat(tokenRegistryService.extractAssetsFromOperations(List.of())).isEmpty();
        }

        @Test
        @DisplayName("Should extract from token bundles and currency metadata")
        void shouldExtractFromBoth() {
            List<Operation> ops = List.of(createOperationWithTokenBundle(), createOperationWithCurrencyMetadata());
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromOperations(ops);
            // token1 + token2 from bundle (policy1), token1 from currency metadata (policy1) — token1/policy1 deduplicates
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should handle operations with null metadata")
        void shouldHandleNullMetadata() {
            Operation op = Operation.builder().metadata(null).amount(null).build();
            assertThat(tokenRegistryService.extractAssetsFromOperations(List.of(op))).isEmpty();
        }
    }

    @Nested
    @DisplayName("fetchMetadataFor* Helper Methods")
    class FetchMetadataHelperTests {

        @Test
        @DisplayName("fetchMetadataForBlockTxList should return empty map for null list")
        void fetchMetadataForBlockTxListNull() {
            assertThat(tokenRegistryService.fetchMetadataForBlockTxList(null)).isEmpty();
        }

        @Test
        @DisplayName("fetchMetadataForBlockTxList should return empty map for empty list")
        void fetchMetadataForBlockTxListEmpty() {
            assertThat(tokenRegistryService.fetchMetadataForBlockTxList(List.of())).isEmpty();
        }

        @Test
        @DisplayName("fetchMetadataForBlockTransactions should return empty for empty")
        void fetchMetadataForBlockTransactionsEmpty() {
            assertThat(tokenRegistryService.fetchMetadataForBlockTransactions(List.of())).isEmpty();
        }

        @Test
        @DisplayName("fetchMetadataForAddressBalances should return empty for empty")
        void fetchMetadataForAddressBalancesEmpty() {
            assertThat(tokenRegistryService.fetchMetadataForAddressBalances(List.of())).isEmpty();
        }

        @Test
        @DisplayName("fetchMetadataForUtxos should return empty for empty")
        void fetchMetadataForUtxosEmpty() {
            assertThat(tokenRegistryService.fetchMetadataForUtxos(List.of())).isEmpty();
        }

        @Test
        @DisplayName("fetchMetadataForUtxos should handle null policyId")
        void fetchMetadataForUtxosNullPolicyId() {
            List<Utxo> utxos = List.of(
                    Utxo.builder().amounts(List.of(createAmt("token1", null, "token1"))).build()
            );
            assertThat(tokenRegistryService.fetchMetadataForUtxos(utxos)).isEmpty();
        }
    }

    // --- Helper methods ---

    private TokenMetadata createCip26Metadata(String subject, String name, String description,
                                               String ticker, Long decimals) {
        TokenMetadata m = new TokenMetadata();
        m.setSubject(subject);
        m.setName(name);
        m.setDescription(description);
        m.setTicker(ticker);
        m.setDecimals(decimals);
        return m;
    }

    private Amt createAmt(String unit, String policyId, String lovelaceOrUnit) {
        return Amt.builder()
                .policyId(policyId)
                .unit(lovelaceOrUnit)
                .quantity(BigDecimal.valueOf(1000000).toBigInteger())
                .build();
    }

    private Utxo createUtxoWithAmounts(List<Amt> amounts) {
        return Utxo.builder().amounts(amounts).build();
    }

    private BlockTx createBlockTxWithInputsAndOutputs() {
        return BlockTx.builder()
                .inputs(List.of(
                        createUtxoWithAmounts(List.of(createAmt("token1", "policy1", "token1"))),
                        createUtxoWithAmounts(List.of(createAmt("token2", "policy2", "token2")))
                ))
                .outputs(List.of(
                        createUtxoWithAmounts(List.of(createAmt("token3", "policy3", "token3"))),
                        createUtxoWithAmounts(List.of(createAmt("token4", "policy4", "token4")))
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

    private BlockTransaction createBlockTransactionWithTokenBundles() {
        List<Amount> tokens1 = List.of(Amount.builder().currency(CurrencyResponse.builder().symbol("token1").build()).value("1000").build());
        List<Amount> tokens2 = List.of(Amount.builder().currency(CurrencyResponse.builder().symbol("token2").build()).value("2000").build());
        TokenBundleItem bundle1 = TokenBundleItem.builder().policyId("policy1").tokens(tokens1).build();
        TokenBundleItem bundle2 = TokenBundleItem.builder().policyId("policy2").tokens(tokens2).build();
        OperationMetadata metadata = OperationMetadata.builder().tokenBundle(List.of(bundle1, bundle2)).build();
        Operation operation = Operation.builder().metadata(metadata).build();
        Transaction transaction = Transaction.builder().operations(List.of(operation)).build();
        return BlockTransaction.builder().transaction(transaction).build();
    }

    private Operation createOperationWithTokenBundle() {
        List<Amount> tokens = List.of(
                Amount.builder().currency(CurrencyResponse.builder().symbol("token1").build()).value("1000").build(),
                Amount.builder().currency(CurrencyResponse.builder().symbol("token2").build()).value("2000").build()
        );
        TokenBundleItem bundle = TokenBundleItem.builder().policyId("policy1").tokens(tokens).build();
        OperationMetadata metadata = OperationMetadata.builder().tokenBundle(List.of(bundle)).build();
        return Operation.builder().metadata(metadata).build();
    }

    private Operation createOperationWithCurrencyMetadata() {
        CurrencyMetadataResponse meta = CurrencyMetadataResponse.builder().policyId("policy1").build();
        CurrencyResponse currency = CurrencyResponse.builder().symbol("token1").metadata(meta).build();
        Amount amount = Amount.builder().currency(currency).value("1000").build();
        return Operation.builder().amount(amount).build();
    }
}
