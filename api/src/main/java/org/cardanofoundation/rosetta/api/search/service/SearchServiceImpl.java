package org.cardanofoundation.rosetta.api.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.openapitools.client.model.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

import static org.openapitools.client.model.Operator.AND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final BlockMapper blockMapper;
    private final LedgerSearchService ledgerSearchService;

    @Override
    @Transactional  // Override class-level readOnly=true for methods that may use temporary tables
    public Page<BlockTransaction> searchTransaction(
            SearchTransactionsRequest searchTransactionsRequest,
            Long offset,
            Long limit) {

        // Validate and normalize success/status parameters
        @Nullable Boolean isSuccess = validateAndNormalizeSuccessStatus(
                searchTransactionsRequest.getSuccess(),
                searchTransactionsRequest.getStatus()
        );

        @Nullable String address = validateAndNormaliseAddress(
                searchTransactionsRequest.getAddress(),
                searchTransactionsRequest.getAccountIdentifier()
        );

        @Nullable String txHash = Optional.ofNullable(searchTransactionsRequest.getTransactionIdentifier()).orElse(
                TransactionIdentifier.builder().build()).getHash();

        // Extract currency for filtering (policy ID or asset identifier)
        @Nullable org.cardanofoundation.rosetta.api.search.model.Currency currency = Optional.ofNullable(searchTransactionsRequest.getCurrency())
                .map(c -> org.cardanofoundation.rosetta.api.search.model.Currency.builder()
                        .symbol(c.getSymbol())
                        .decimals(c.getDecimals())
                        .policyId(Optional.ofNullable(c.getMetadata()).map(CurrencyMetadata::getPolicyId).orElse(null))
                        .build())
                .orElse(null);

        @Nullable Long maxBlock = searchTransactionsRequest.getMaxBlock();

        @Nullable UtxoKey utxoKey = Optional.ofNullable(searchTransactionsRequest.getCoinIdentifier())
                .flatMap(extractUTxOFromCoinIdentifier())
                .orElse(null);

        Operator operator = Optional.ofNullable(searchTransactionsRequest.getOperator())
                .orElse(AND);

        BlockIdentifier blockIdentifier = Optional.ofNullable(searchTransactionsRequest.getBlockIdentifier())
                .orElse(BlockIdentifier.builder().build());

        Page<BlockTx> blockTxes = ledgerSearchService.searchTransaction(
                operator,
                txHash,
                address,
                utxoKey,
                currency,
                blockIdentifier.getHash(),
                blockIdentifier.getIndex(),
                maxBlock,
                isSuccess,
                offset,
                limit
        );

        return blockTxes.map(blockMapper::mapToBlockTransaction);
    }

    static Function<CoinIdentifier, Optional<UtxoKey>> extractUTxOFromCoinIdentifier() {
        return coinIdentifier -> {
            if (ObjectUtils.isNotEmpty(coinIdentifier.getIdentifier())) {
                String[] split = coinIdentifier.getIdentifier().split(":");

                String txHash_ = split[0];
                int outputIndex_ = Integer.parseInt(split[1]);

                return Optional.of(new UtxoKey(txHash_, outputIndex_));
            }

            return Optional.empty();
        };
    }

    @Nullable
    Boolean validateAndNormalizeSuccessStatus(@Nullable Boolean success,
                                              @Nullable String status) {
        log.info("Validating success and status parameters: success={}, status={}", success, status);

        if (success != null && status != null) {
            throw ExceptionFactory.bothSuccessAndStatusProvided();
        }

        if (status != null) {
            return "success".equalsIgnoreCase(status);
        }

        return success;
    }

    @Nullable
    String validateAndNormaliseAddress(@Nullable String address,
                                       @Nullable AccountIdentifier accountIdentifier) {
        if (address != null && accountIdentifier != null) {
            throw ExceptionFactory.bothAccountAndAccountIdentifierProvided();
        }

        if (address != null) {
            return address;
        }

        return Optional.ofNullable(accountIdentifier)
                .map(AccountIdentifier::getAddress)
                .orElse(null);
    }

}
