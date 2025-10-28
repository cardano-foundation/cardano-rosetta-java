package org.cardanofoundation.rosetta.api.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.service.TokenRegistryService;
import org.cardanofoundation.rosetta.api.search.model.Operator;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.openapitools.client.model.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final BlockMapper blockMapper;
    private final LedgerSearchService ledgerSearchService;
    private final TokenRegistryService tokenRegistryService;

    @Override
    @Transactional  // Override class-level readOnly=true for methods that may use temporary tables
    public Page<BlockTransaction> searchTransaction(
            SearchTransactionsRequest searchTransactionsRequest,
            Long offset,
            Long limit) {

        Optional.ofNullable(searchTransactionsRequest.getType()).ifPresent(type -> {
            throw ExceptionFactory.operationTypeSearchNotSupported(type);
        });

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
                        .policyId(Optional.ofNullable(c.getMetadata()).map(CurrencyMetadataRequest::getPolicyId).orElse(null))
                        .build())
                .orElse(null);

        @Nullable Long maxBlock = searchTransactionsRequest.getMaxBlock();

        @Nullable UtxoKey utxoKey = Optional.ofNullable(searchTransactionsRequest.getCoinIdentifier())
                .flatMap(extractUTxOFromCoinIdentifier())
                .orElse(null);

        // Parse and validate operator
        Operator operator = parseAndValidateOperator(searchTransactionsRequest.getOperator());

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

        // Always fetch metadata for all transactions in this page (will be empty map if no native tokens)
        final Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap =
            tokenRegistryService.fetchMetadataForBlockTxList(blockTxes.getContent());

        // Always use the metadata version (with empty map when no native tokens)
        return blockTxes.map(tx ->
            blockMapper.mapToBlockTransactionWithMetadata(tx, metadataMap));
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
        if (success != null && status != null) {
            throw ExceptionFactory.bothSuccessAndStatusProvided();
        }

        if (status != null) {
            if ("success".equalsIgnoreCase(status) || "true".equalsIgnoreCase(status)) {
                return true;
            }
            if ("invalid".equalsIgnoreCase(status) || "false".equalsIgnoreCase(status)) {
                return false;
            }

            throw ExceptionFactory.invalidOperationStatus(status);
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

    private Operator parseAndValidateOperator(@Nullable String operatorString) {
        if (operatorString == null || operatorString.isEmpty()) {
            // Default to AND if not specified
            return Operator.AND;
        }

        try {
            return Operator.valueOf(operatorString.toUpperCase());
        } catch (IllegalArgumentException e) {
            String details = String.format("Unknown operator: '%s'. Supported values are: 'AND', 'OR'", operatorString);
            throw ExceptionFactory.unspecifiedErrorNotRetriable(details);
        }
    }

}
