package org.cardanofoundation.rosetta.api.search.service;

import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.common.exception.ApiException;
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
import java.util.Optional;

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
    class AccountValidationTests {

        @Test
        void shouldThrowException_whenBothAddressAndAccountIdentifierProvided() {
            // Given
            AccountIdentifier accountId = AccountIdentifier.builder().address("addr_from_account").build();

            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .address("addr_from_request")
                    .accountIdentifier(accountId)
                    .build();

            // When & Then
            assertThatThrownBy(() -> searchService.searchTransaction(request, 0L, 10L))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Cannot specify both 'account' and 'accountIdentifier' parameters simultaneously");

            verifyNoInteractions(ledgerSearchService);
        }

        @Test
        void shouldAcceptOnlyAddressParameter() {
            // Given
            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .address("test_address")
                    .build();

            Page<BlockTx> mockBlockTxPage = new PageImpl<>(List.of());

            when(ledgerSearchService.searchTransaction(any(), any(), eq("test_address"), any(), any(), any(), any(), any(), any(), anyLong(), anyLong()))
                    .thenReturn(mockBlockTxPage);

            // When
            searchService.searchTransaction(request, 0L, 10L);

            // Then
            verify(ledgerSearchService).searchTransaction(
                    any(), any(),
                    eq("test_address"), // Should use the address parameter
                    any(), any(), any(), any(), any(), any(),
                    anyLong(), anyLong()
            );
        }

        @Test
        void shouldAcceptOnlyAccountIdentifierParameter() {
            // Given
            AccountIdentifier accountId = AccountIdentifier.builder().address("account_address").build();

            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .accountIdentifier(accountId)
                    .build();

            Page<BlockTx> mockBlockTxPage = new PageImpl<>(List.of());

            when(ledgerSearchService.searchTransaction(any(), any(), eq("account_address"), any(), any(), any(), any(), any(), any(), anyLong(), anyLong()))
                    .thenReturn(mockBlockTxPage);

            // When
            searchService.searchTransaction(request, 0L, 10L);

            // Then
            verify(ledgerSearchService).searchTransaction(
                    any(), any(),
                    eq("account_address"), // Should use address from accountIdentifier
                    any(), any(), any(), any(), any(), any(),
                    anyLong(), anyLong()
            );
        }

        @Test
        void shouldPassNullWhenNeitherAddressNorAccountIdentifierProvided() {
            // Given
            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .build();

            Page<BlockTx> mockBlockTxPage = new PageImpl<>(List.of());

            when(ledgerSearchService.searchTransaction(any(), any(), isNull(), any(), any(), any(), any(), any(), any(), anyLong(), anyLong()))
                    .thenReturn(mockBlockTxPage);

            // When
            searchService.searchTransaction(request, 0L, 10L);

            // Then
            verify(ledgerSearchService).searchTransaction(
                    any(), any(),
                    isNull(), // Should pass null when neither is provided
                    any(), any(), any(), any(), any(), any(),
                    anyLong(), anyLong()
            );
        }
    }

    @Nested
    class CurrencySearchTests {

        @Test
        void shouldThrowException_whenCurrencyIsProvided() {
            // Given
            Currency currency = Currency.builder().symbol("ADA").build();

            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .currency(currency)
                    .build();

            // When & Then
            assertThatThrownBy(() -> searchService.searchTransaction(request, 0L, 10L))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Currency search is not currently supported");

            verifyNoInteractions(ledgerSearchService);
        }
    }

    @Nested
    class ValidateAndNormalizeSuccessStatusTests {

        @Test
        void shouldThrowException_whenBothSuccessAndStatusProvided() {
            // When & Then
            assertThatThrownBy(() -> searchService.validateAndNormalizeSuccessStatus(true, "success"))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Cannot specify both 'success' and 'status' parameters simultaneously");
        }

        @Test
        void shouldReturnSuccess_whenOnlySuccessParameterProvidedTrue() {
            // When
            Boolean result = searchService.validateAndNormalizeSuccessStatus(true, null);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalse_whenOnlySuccessParameterProvidedFalse() {
            // When
            Boolean result = searchService.validateAndNormalizeSuccessStatus(false, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnTrue_whenStatusIsSuccess() {
            // When
            Boolean result = searchService.validateAndNormalizeSuccessStatus(null, "success");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnTrue_whenStatusIsSuccessIgnoreCase() {
            // When
            Boolean result = searchService.validateAndNormalizeSuccessStatus(null, "SUCCESS");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalse_whenStatusIsNotSuccess() {
            // When
            Boolean result = searchService.validateAndNormalizeSuccessStatus(null, "failed");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenStatusIsFailIgnoreCase() {
            // When
            Boolean result = searchService.validateAndNormalizeSuccessStatus(null, "FAILED");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnNull_whenBothParametersAreNull() {
            // When
            Boolean result = searchService.validateAndNormalizeSuccessStatus(null, null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    class ValidateAndNormaliseAddressTests {

        @Test
        void shouldThrowException_whenBothAddressAndAccountIdentifierProvided() {
            // Given
            AccountIdentifier accountId = AccountIdentifier.builder().address("account_address").build();

            // When & Then
            assertThatThrownBy(() -> searchService.validateAndNormaliseAddress("direct_address", accountId))
                    .isInstanceOf(ApiException.class)
                    .hasMessage("Cannot specify both 'account' and 'accountIdentifier' parameters simultaneously");
        }

        @Test
        void shouldReturnAddress_whenOnlyAddressParameterProvided() {
            // When
            String result = searchService.validateAndNormaliseAddress("test_address", null);

            // Then
            assertThat(result).isEqualTo("test_address");
        }

        @Test
        void shouldReturnAccountAddress_whenOnlyAccountIdentifierProvided() {
            // Given
            AccountIdentifier accountId = AccountIdentifier.builder().address("account_address").build();

            // When
            String result = searchService.validateAndNormaliseAddress(null, accountId);

            // Then
            assertThat(result).isEqualTo("account_address");
        }

        @Test
        void shouldReturnNull_whenBothParametersAreNull() {
            // When
            String result = searchService.validateAndNormaliseAddress(null, null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void shouldReturnNull_whenAccountIdentifierHasNullAddress() {
            // Given
            AccountIdentifier accountId = AccountIdentifier.builder().address(null).build();

            // When
            String result = searchService.validateAndNormaliseAddress(null, accountId);

            // Then
            assertThat(result).isNull();
        }

        @Test
        void shouldReturnNull_whenAccountIdentifierIsNullObject() {
            // When
            String result = searchService.validateAndNormaliseAddress(null, null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    class GetCoinIdentifierUtxoKeyFunctionTests {

        @Test
        void shouldReturnUtxoKey_whenValidIdentifierProvided() {
            // Given
            CoinIdentifier coinId = CoinIdentifier.builder().identifier("tx123:5").build();
            var function = SearchServiceImpl.extractUTxOFromCoinIdentifier();

            // When
            Optional<UtxoKey> result = function.apply(coinId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTxHash()).isEqualTo("tx123");
            assertThat(result.get().getOutputIndex()).isEqualTo(5);
        }

        @Test
        void shouldReturnUtxoKey_whenIdentifierWithZeroIndex() {
            // Given
            CoinIdentifier coinId = CoinIdentifier.builder().identifier("abcdef123456:0").build();
            var function = SearchServiceImpl.extractUTxOFromCoinIdentifier();

            // When
            Optional<UtxoKey> result = function.apply(coinId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTxHash()).isEqualTo("abcdef123456");
            assertThat(result.get().getOutputIndex()).isEqualTo(0);
        }

        @Test
        void shouldReturnUtxoKey_whenIdentifierWithLargeIndex() {
            // Given
            CoinIdentifier coinId = CoinIdentifier.builder().identifier("hash:999").build();
            var function = SearchServiceImpl.extractUTxOFromCoinIdentifier();

            // When
            Optional<UtxoKey> result = function.apply(coinId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTxHash()).isEqualTo("hash");
            assertThat(result.get().getOutputIndex()).isEqualTo(999);
        }

        @Test
        void shouldReturnEmpty_whenIdentifierIsNull() {
            // Given
            CoinIdentifier coinId = CoinIdentifier.builder().identifier(null).build();
            var function = SearchServiceImpl.extractUTxOFromCoinIdentifier();

            // When
            Optional<UtxoKey> result = function.apply(coinId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenIdentifierIsEmpty() {
            // Given
            CoinIdentifier coinId = CoinIdentifier.builder().identifier("").build();
            var function = SearchServiceImpl.extractUTxOFromCoinIdentifier();

            // When
            Optional<UtxoKey> result = function.apply(coinId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldThrowException_whenIdentifierIsBlank() {
            // Given
            CoinIdentifier coinId = CoinIdentifier.builder().identifier("   ").build();
            var function = SearchServiceImpl.extractUTxOFromCoinIdentifier();

            // When & Then - Blank string will be treated as non-empty by ObjectUtils but will fail parsing
            assertThatThrownBy(() -> function.apply(coinId))
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }

        @Test
        void shouldThrowException_whenIdentifierHasInvalidFormat() {
            // Given
            CoinIdentifier coinId = CoinIdentifier.builder().identifier("invalidformat").build();
            var function = SearchServiceImpl.extractUTxOFromCoinIdentifier();

            // When & Then
            assertThatThrownBy(() -> function.apply(coinId))
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }

        @Test
        void shouldThrowException_whenIdentifierHasNonNumericIndex() {
            // Given
            CoinIdentifier coinId = CoinIdentifier.builder().identifier("tx123:abc").build();
            var function = SearchServiceImpl.extractUTxOFromCoinIdentifier();

            // When & Then
            assertThatThrownBy(() -> function.apply(coinId))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        void shouldHandleMultipleColons_takingFirstAsDelimiter() {
            // Given
            CoinIdentifier coinId = CoinIdentifier.builder().identifier("tx:hash:123:456").build();
            var function = SearchServiceImpl.extractUTxOFromCoinIdentifier();

            // When & Then - This should throw NumberFormatException because "hash" is not a number
            assertThatThrownBy(() -> function.apply(coinId))
                    .isInstanceOf(NumberFormatException.class);
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
            BlockIdentifier blockId = BlockIdentifier.builder().hash("block123").index(100L).build();

            SearchTransactionsRequest request = SearchTransactionsRequest.builder()
                    .networkIdentifier(networkIdentifier)
                    .transactionIdentifier(txId)
                    .accountIdentifier(accountId)
                    .coinIdentifier(coinId)
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
                    isNull(), // currency is not supported, so null is passed
                    eq("block123"),
                    eq(100L),
                    eq(200L),
                    eq(false),
                    eq(5L),
                    eq(15L)
            );
        }

    }
}