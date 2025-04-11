package org.cardanofoundation.rosetta.api.search.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.ObjectUtils;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;

import static org.openapitools.client.model.Operator.AND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final BlockMapper blockMapper;
    private final LedgerSearchService ledgerSearchService;

    @Override
    public Slice<BlockTransaction> searchTransaction(
            SearchTransactionsRequest searchTransactionsRequest,
            Long offset,
            Long pageSize) {

        // Using address either from searchTransactionsRequest.address or from searchTransactionsRequest.accountIdentifier.address
        String address = Optional.ofNullable(searchTransactionsRequest.getAddress())
                .orElse(Optional.ofNullable(searchTransactionsRequest.getAccountIdentifier()).orElse(
                        AccountIdentifier.builder().build()).getAddress());

        String txHash = Optional.ofNullable(searchTransactionsRequest.getTransactionIdentifier()).orElse(
                TransactionIdentifier.builder().build()).getHash();

        String symbol = Optional.ofNullable(searchTransactionsRequest.getCurrency())
                .orElse(Currency.builder().build()).getSymbol();

        Long maxBlock = searchTransactionsRequest.getMaxBlock();

        UtxoKey utxoKey = Optional.ofNullable(searchTransactionsRequest.getCoinIdentifier())
                .map(coinIdentifier -> {
                    if (ObjectUtils.isNotEmpty(coinIdentifier.getIdentifier())) {
                        String[] split = coinIdentifier.getIdentifier().split(":");

                        return new UtxoKey(split[0], Integer.parseInt(split[1]));
                    }
                    return null;
                }).orElse(null);

        Operator operator = Optional.ofNullable(searchTransactionsRequest.getOperator()).orElse(AND);

        BlockIdentifier blockIdentifier = Optional.ofNullable(
                        searchTransactionsRequest.getBlockIdentifier())
                .orElse(BlockIdentifier.builder()
                        .build());

        Slice<BlockTx> blockTxes = ledgerSearchService.searchTransaction(
                operator,
                txHash,
                address,
                utxoKey,
                symbol,
                blockIdentifier.getHash(),
                blockIdentifier.getIndex(),
                maxBlock,
                offset.intValue(),
                pageSize.intValue()
        );

        return blockTxes.map(blockMapper::mapToBlockTransaction);
    }

}
