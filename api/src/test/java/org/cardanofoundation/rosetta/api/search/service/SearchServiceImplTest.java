package org.cardanofoundation.rosetta.api.search.service;

import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private BlockMapper blockMapper;

    @Mock
    private LedgerSearchService ledgerSearchService;

    @InjectMocks
    private SearchServiceImpl searchService;

    private SearchTransactionsRequest baseRequest;
    private NetworkIdentifier networkIdentifier;

    @BeforeEach
    void setUp() {
        networkIdentifier = NetworkIdentifier.builder()
                .blockchain("cardano")
                .network("testnet")
                .build();

        baseRequest = SearchTransactionsRequest.builder()
                .networkIdentifier(networkIdentifier)
                .build();
    }

    @Nested
    class SuccessAndStatusValidationTests {

        @Test
        void shouldThrowException_whenBothSuccessAndStatusProvided() {
            // Given
            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .success(true)
                    .status("success")
                    .build();

            // When & Then
            assertThatThrownBy(() -> searchService.searchTransaction(request, 0L, 10L))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Cannot specify both 'success' and 'status' parameters simultaneously");

            verifyNoInteractions(ledgerSearchService);
        }

        @Test
        void shouldAcceptOnlySuccessParameter() {
            // Given
            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .success(true)
                    .build();

            Page<BlockTx> mockBlockTxPage = new PageImpl<>(List.of());

            when(ledgerSearchService.searchTransaction(any(), any(), any(), any(), any(), any(), any(), any(), eq(true), anyLong(), anyLong()))
                    .thenReturn(mockBlockTxPage);

            // When
            Page<BlockTransaction> result = searchService.searchTransaction(request, 0L, 10L);

            // Then
            verify(ledgerSearchService).searchTransaction(
                    any(), any(), any(), any(), any(), any(), any(), any(),
                    eq(true), // isSuccess should be true
                    eq(0L), eq(10L)
            );
            assertThat(result).isNotNull();
        }

        @Test
        void shouldAcceptOnlyStatusParameter_success() {
            // Given
            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .status("success")
                    .build();

            Page<BlockTx> mockBlockTxPage = new PageImpl<>(List.of());

            when(ledgerSearchService.searchTransaction(any(), any(), any(), any(), any(), any(), any(), any(), eq(true), anyLong(), anyLong()))
                    .thenReturn(mockBlockTxPage);

            // When
            searchService.searchTransaction(request, 0L, 10L);

            // Then
            verify(ledgerSearchService).searchTransaction(
                    any(), any(), any(), any(), any(), any(), any(), any(),
                    eq(true), // status "success" should convert to isSuccess = true
                    eq(0L), eq(10L)
            );
        }

        @Test
        void shouldAcceptOnlyStatusParameter_fail() {
            // Given
            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .status("fail")
                    .build();

            Page<BlockTx> mockBlockTxPage = new PageImpl<>(List.of());

            when(ledgerSearchService.searchTransaction(any(), any(), any(), any(), any(), any(), any(), any(), eq(false), anyLong(), anyLong()))
                    .thenReturn(mockBlockTxPage);

            // When
            searchService.searchTransaction(request, 0L, 10L);

            // Then
            verify(ledgerSearchService).searchTransaction(
                    any(), any(), any(), any(), any(), any(), any(), any(),
                    eq(false), // status "fail" should convert to isSuccess = false
                    eq(0L), eq(10L)
            );
        }

        @Test
        void shouldAcceptStatusParameter_caseInsensitive() {
            // Given
            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .status("SUCCESS")
                    .build();

            Page<BlockTx> mockBlockTxPage = new PageImpl<>(List.of());

            when(ledgerSearchService.searchTransaction(any(), any(), any(), any(), any(), any(), any(), any(), eq(true), anyLong(), anyLong()))
                    .thenReturn(mockBlockTxPage);

            // When
            searchService.searchTransaction(request, 0L, 10L);

            // Then
            verify(ledgerSearchService).searchTransaction(
                    any(), any(), any(), any(), any(), any(), any(), any(),
                    eq(true), // "SUCCESS" should convert to isSuccess = true
                    eq(0L), eq(10L)
            );
        }

        @Test
        void shouldPassNullWhenNeitherSuccessNorStatusProvided() {
            // Given
            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .build();

            Page<BlockTx> mockBlockTxPage = new PageImpl<>(List.of());

            when(ledgerSearchService.searchTransaction(any(), any(), any(), any(), any(), any(), any(), any(), isNull(), anyLong(), anyLong()))
                    .thenReturn(mockBlockTxPage);

            // When
            searchService.searchTransaction(request, 0L, 10L);

            // Then
            verify(ledgerSearchService).searchTransaction(
                    any(), any(), any(), any(), any(), any(), any(), any(),
                    isNull(), // should pass null when neither parameter is provided
                    eq(0L), eq(10L)
            );
        }
    }

    @Nested
    class ParameterMappingTests {

        @Test
        void shouldMapAllParametersCorrectly() {
            // Given
            TransactionIdentifier txId = TransactionIdentifier.builder().hash("tx123").build();
            AccountIdentifier accountId = AccountIdentifier.builder().address("addr123").build();
            CoinIdentifier coinId = CoinIdentifier.builder().identifier("tx123:0").build();
            Currency currency = Currency.builder().symbol("ADA").build();
            BlockIdentifier blockId = BlockIdentifier.builder().hash("block123").index(100L).build();

            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .transactionIdentifier(txId)
                    .accountIdentifier(accountId)
                    .coinIdentifier(coinId)
                    .currency(currency)
                    .blockIdentifier(blockId)
                    .maxBlock(200L)
                    .operator(Operator.OR)
                    .success(false)
                    .build();

            Page<BlockTx> mockBlockTxPage = new PageImpl<>(List.of());

            when(ledgerSearchService.searchTransaction(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyLong(), anyLong()))
                    .thenReturn(mockBlockTxPage);

            // When
            searchService.searchTransaction(request, 5L, 15L);

            // Then
            verify(ledgerSearchService).searchTransaction(
                    eq(Operator.OR),
                    eq("tx123"),
                    eq("addr123"),
                    argThat(utxoKey -> utxoKey.getTxHash().equals("tx123") && utxoKey.getOutputIndex().equals(0)),
                    eq("ADA"),
                    eq("block123"),
                    eq(100L),
                    eq(200L),
                    eq(false),
                    eq(5L),
                    eq(15L)
            );
        }

        @Test
        void shouldUseAddressFromRequestWhenBothAddressAndAccountIdentifierProvided() {
            // Given
            AccountIdentifier accountId = AccountIdentifier.builder().address("addr_from_account").build();

            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .address("addr_from_request")
                    .accountIdentifier(accountId)
                    .build();

            Page<BlockTx> mockBlockTxPage = new PageImpl<>(List.of());

            when(ledgerSearchService.searchTransaction(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyLong(), anyLong()))
                    .thenReturn(mockBlockTxPage);

            // When
            searchService.searchTransaction(request, 0L, 10L);

            // Then
            verify(ledgerSearchService).searchTransaction(
                    any(),
                    any(),
                    eq("addr_from_request"), // Should use address from request, not accountIdentifier
                    any(), any(), any(), any(), any(), any(),
                    anyLong(), anyLong()
            );
        }
    }
}