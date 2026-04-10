package org.cardanofoundation.rosetta.api.common.service;

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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenRegistryServiceImpl Tests")
class TokenRegistryServiceImplTest {

    @Mock
    private TokenQueryServiceImpl tokenQueryService;

    private TokenRegistryServiceImpl tokenRegistryService;

    private static final String POLICY_ID = "a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3e4f5a0b1c2d3";
    private static final String ASSET_SYMBOL_HEX = "54657374546f6b656e";
    private static final String SUBJECT = POLICY_ID + ASSET_SYMBOL_HEX;

    @BeforeEach
    void setUp() {
        tokenRegistryService = new TokenRegistryServiceImpl(tokenQueryService);
    }

    @Nested
    @DisplayName("getTokenMetadataBatch Tests")
    class GetTokenMetadataBatchTests {

        @Test
        @DisplayName("Should return empty map when assets set is empty")
        void shouldReturnEmptyMapForEmptyAssets() {
            assertThat(tokenRegistryService.getTokenMetadataBatch(Set.of())).isEmpty();
            verifyNoInteractions(tokenQueryService);
        }

        @Test
        @DisplayName("Should delegate to TokenQueryService and return results")
        void shouldDelegateToQueryService() {
            AssetFingerprint asset = AssetFingerprint.of(POLICY_ID, ASSET_SYMBOL_HEX);

            TokenRegistryCurrencyData data = TokenRegistryCurrencyData.builder()
                    .policyId(POLICY_ID).subject(SUBJECT).name("Test Token").decimals(6).build();
            when(tokenQueryService.queryMetadataBatch(anyCollection()))
                    .thenReturn(Map.of(asset, data));

            Map<AssetFingerprint, TokenRegistryCurrencyData> result =
                    tokenRegistryService.getTokenMetadataBatch(Set.of(asset));

            assertThat(result).containsEntry(asset, data);
            verify(tokenQueryService).queryMetadataBatch(Set.of(asset));
        }
    }

    @Nested
    @DisplayName("Asset Extraction Tests")
    class AssetExtractionTests {

        @Test
        void shouldExtractFromBlockTx() {
            BlockTx blockTx = createBlockTxWithInputsAndOutputs();
            assertThat(tokenRegistryService.extractAssetsFromBlockTx(blockTx)).hasSize(4);
        }

        @Test
        void shouldExcludeLovelace() {
            BlockTx blockTx = createBlockTxWithLovelaceAndTokens();
            Set<AssetFingerprint> result = tokenRegistryService.extractAssetsFromBlockTx(blockTx);
            assertThat(result).hasSize(1).contains(AssetFingerprint.of("policy1", "token1"));
        }

        @Test
        void shouldHandleEmptyAmounts() {
            assertThat(tokenRegistryService.extractAssetsFromAmounts(List.of())).isEmpty();
        }

        @Test
        void shouldHandleNullAmounts() {
            Utxo utxo = Utxo.builder().amounts(null).build();
            BlockTx blockTx = BlockTx.builder().inputs(List.of(utxo)).build();
            assertThat(tokenRegistryService.extractAssetsFromBlockTx(blockTx)).isEmpty();
        }

        @Test
        void shouldExtractFromBlockTransactions() {
            assertThat(tokenRegistryService.extractAssetsFromBlockTransactions(List.of())).isEmpty();
            List<BlockTransaction> txs = List.of(createBlockTransactionWithTokenBundles());
            assertThat(tokenRegistryService.extractAssetsFromBlockTransactions(txs)).hasSize(2);
        }

        @Test
        void shouldHandleNullMetadata() {
            Operation op = Operation.builder().metadata(null).amount(null).build();
            assertThat(tokenRegistryService.extractAssetsFromOperations(List.of(op))).isEmpty();
        }
    }

    @Nested
    @DisplayName("fetchMetadataFor* Helper Methods")
    class FetchMetadataHelperTests {

        @Test
        void fetchMetadataForBlockTxListNull() {
            assertThat(tokenRegistryService.fetchMetadataForBlockTxList(null)).isEmpty();
        }

        @Test
        void fetchMetadataForBlockTxListEmpty() {
            assertThat(tokenRegistryService.fetchMetadataForBlockTxList(List.of())).isEmpty();
        }

        @Test
        void fetchMetadataForBlockTransactionsEmpty() {
            assertThat(tokenRegistryService.fetchMetadataForBlockTransactions(List.of())).isEmpty();
        }

        @Test
        void fetchMetadataForAddressBalancesEmpty() {
            assertThat(tokenRegistryService.fetchMetadataForAddressBalances(List.of())).isEmpty();
        }

        @Test
        void fetchMetadataForUtxosEmpty() {
            assertThat(tokenRegistryService.fetchMetadataForUtxos(List.of())).isEmpty();
        }

        @Test
        void fetchMetadataForUtxosNullPolicyId() {
            List<Utxo> utxos = List.of(
                    Utxo.builder().amounts(List.of(createAmt("token1", null, "token1"))).build()
            );
            assertThat(tokenRegistryService.fetchMetadataForUtxos(utxos)).isEmpty();
        }
    }

    // --- Helpers ---

    private Amt createAmt(String unit, String policyId, String lovelaceOrUnit) {
        return Amt.builder().policyId(policyId).unit(lovelaceOrUnit)
                .quantity(BigDecimal.valueOf(1000000).toBigInteger()).build();
    }

    private Utxo createUtxoWithAmounts(List<Amt> amounts) {
        return Utxo.builder().amounts(amounts).build();
    }

    private BlockTx createBlockTxWithInputsAndOutputs() {
        return BlockTx.builder()
                .inputs(List.of(
                        createUtxoWithAmounts(List.of(createAmt("token1", "policy1", "token1"))),
                        createUtxoWithAmounts(List.of(createAmt("token2", "policy2", "token2")))))
                .outputs(List.of(
                        createUtxoWithAmounts(List.of(createAmt("token3", "policy3", "token3"))),
                        createUtxoWithAmounts(List.of(createAmt("token4", "policy4", "token4")))))
                .build();
    }

    private BlockTx createBlockTxWithLovelaceAndTokens() {
        return BlockTx.builder()
                .inputs(List.of(createUtxoWithAmounts(List.of(
                        createAmt(LOVELACE, null, LOVELACE),
                        createAmt("token1", "policy1", "token1")))))
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
}
