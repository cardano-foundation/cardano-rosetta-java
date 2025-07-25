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
    public Page<BlockTransaction> searchTransaction(
            SearchTransactionsRequest searchTransactionsRequest,
            Long offset,
            Long limit) {

        // Validate and normalize success/status parameters
        Boolean isSuccess = validateAndNormalizeSuccessStatus(
                searchTransactionsRequest.getSuccess(),
                searchTransactionsRequest.getStatus()
        );

        // Using address either from searchTransactionsRequest.address or from searchTransactionsRequest.accountIdentifier.address
        String address = Optional.ofNullable(searchTransactionsRequest.getAddress())
                .orElse(Optional.ofNullable(searchTransactionsRequest.getAccountIdentifier()).orElse(
                        AccountIdentifier.builder().build()).getAddress());

        String txHash = Optional.ofNullable(searchTransactionsRequest.getTransactionIdentifier()).orElse(
                TransactionIdentifier.builder().build()).getHash();

        String currencySymbol = Optional.ofNullable(searchTransactionsRequest.getCurrency())
                .orElse(Currency.builder().build())
                .getSymbol();

        Long maxBlock = searchTransactionsRequest.getMaxBlock();

        UtxoKey utxoKey = Optional.ofNullable(searchTransactionsRequest.getCoinIdentifier())
                .map(getCoinIdentifierUtxoKeyFunction())
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
                currencySymbol,
                blockIdentifier.getHash(),
                blockIdentifier.getIndex(),
                maxBlock,
                isSuccess,
                offset,
                limit
        );

        return blockTxes.map(blockMapper::mapToBlockTransaction);
    }

    private static Function<CoinIdentifier, UtxoKey> getCoinIdentifierUtxoKeyFunction() {
        return coinIdentifier -> {
            if (ObjectUtils.isNotEmpty(coinIdentifier.getIdentifier())) {
                String[] split = coinIdentifier.getIdentifier().split(":");

                String txHash_ = split[0];
                int outputIndex_ = Integer.parseInt(split[1]);

                return new UtxoKey(txHash_, outputIndex_);
            }

            return null;
        };
    }

    /**
     * Validates and normalizes success/status parameters.
     * If both are provided, throws an exception.
     * If status is provided, converts to boolean (success -> true, fail -> false).
     * If success is provided, returns it directly.
     * If neither is provided, returns null.
     */
    private Boolean validateAndNormalizeSuccessStatus(Boolean success, String status) {
        log.info("Validating success and status parameters: success={}, status={}", success, status);

        if (success != null && status != null) {
            throw ExceptionFactory.bothSuccessAndStatusProvided();
        }

        if (status != null) {
            return "success".equalsIgnoreCase(status);
        }
        
        return success;
    }

}
