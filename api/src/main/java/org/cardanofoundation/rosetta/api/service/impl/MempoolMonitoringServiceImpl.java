package org.cardanofoundation.rosetta.api.service.impl;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
import com.bloxbean.cardano.yaci.core.util.HexUtil;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.UnknownHostException;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.TransactionExtraData;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.TransactionParsed;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.MempoolMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class MempoolMonitoringServiceImpl implements MempoolMonitoringService {

    private final CardanoService cardanoService;
    private final LocalTxMonitorClient localTxMonitorClient;
    @Autowired
    @Qualifier("redisTemplateString")
    private final RedisTemplate<String, String> redisTemplate;

    public MempoolMonitoringServiceImpl(CardanoService cardanoService,
                                        LocalTxMonitorClient localTxMonitorClient,
                                        @Qualifier("redisTemplateString") RedisTemplate<String, String> redisTemplate) {
        this.cardanoService = cardanoService;
        this.localTxMonitorClient = localTxMonitorClient;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public MempoolResponse getAllTransaction(NetworkRequest networkRequest) {

        Set<String> txHashes = getAllTransactionsInMempool();
        log.info("[allTransaction] Looking for all transaction in mempool" + txHashes);
        List<TransactionIdentifier> transactionIdentifierList =
                txHashes.stream()
                        .map(txHash ->
                                new TransactionIdentifier(txHash.substring(Constants.REDIS_PREFIX_MEMPOOL.length()))
                        ).toList();
        MempoolResponse mempoolResponse = MempoolResponse.builder()
                .transactionIdentifierList(transactionIdentifierList)
                .build();
        return mempoolResponse;
    }

    @Override
    public MempoolTransactionResponse getDetailTransaction(
            MempoolTransactionRequest mempoolTransactionRequest)
            throws CborException, CborDeserializationException, UnknownHostException, AddressExcepion, CborSerializationException, JsonProcessingException {
        String txHash = mempoolTransactionRequest.getTransactionIdentifier().getHash();
        String txData = redisTemplate.opsForValue().get(Constants.REDIS_PREFIX_MEMPOOL + txHash);
        if (Objects.isNull(txData)) {
            return null;
        }
        log.info("Tx data for txHash {} is {}",txHash, txData);
        Array array = cardanoService.decodeExtraData(txData);
        TransactionExtraData extraData = cardanoService.changeFromMaptoObject(
                (Map) array.getDataItems().get(1));
        log.info(array + "[constructionParse] Decoded");
        TransactionParsed result;
        NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(
                mempoolTransactionRequest.getNetworkIdentifier());
        result = cardanoService.parseSignedTransaction(networkIdentifier,
                ((UnicodeString) array.getDataItems().get(0)).getString(), extraData);
        MempoolTransactionResponse mempoolTransactionResponse = MempoolTransactionResponse.builder().
                transaction(
                        new org.cardanofoundation.rosetta.api.model.Transaction(
                                new TransactionIdentifier(txHash),
                                result.getOperations())
                )
                .build();
        return mempoolTransactionResponse;
    }

    @Override
    public Set<String> getAllTransactionsInMempool() {
        return redisTemplate.keys("mempool*");
    }
}
