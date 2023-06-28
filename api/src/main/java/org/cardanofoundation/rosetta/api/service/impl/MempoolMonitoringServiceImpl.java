package org.cardanofoundation.rosetta.api.service.impl;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.model.TransactionExtraData;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.TransactionParsed;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.MempoolMonitoringService;
import org.cardanofoundation.rosetta.api.util.DataItemDecodeUtil;
import org.cardanofoundation.rosetta.api.util.ParseConstructionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MempoolMonitoringServiceImpl implements MempoolMonitoringService {

  private final CardanoService cardanoService;
  private final RedisTemplate<String, String> redisTemplate;

  public MempoolMonitoringServiceImpl(CardanoService cardanoService,
      @Qualifier("redisTemplateString") RedisTemplate<String, String> redisTemplate) {
    this.cardanoService = cardanoService;
    this.redisTemplate = redisTemplate;
  }

  @Override
  public MempoolResponse getAllTransaction(NetworkRequest networkRequest) {

    Set<String> txHashes = getAllTransactionsInMempool();
    log.info("[allTransaction] Looking for all transaction in mempool" + txHashes);
    List<TransactionIdentifier> transactionIdentifierList = txHashes.stream().map(
        txHash -> new TransactionIdentifier(
            txHash.substring(Constants.REDIS_PREFIX_MEMPOOL.length()))).toList();
    MempoolResponse mempoolResponse = MempoolResponse.builder()
        .transactionIdentifierList(transactionIdentifierList).build();
    return mempoolResponse;
  }

  @Override
  public MempoolTransactionResponse getDetailTransaction(
      MempoolTransactionRequest mempoolTransactionRequest) {
    String txHash = mempoolTransactionRequest.getTransactionIdentifier().getHash();
    String txData = redisTemplate.opsForValue().get(Constants.REDIS_PREFIX_MEMPOOL + txHash);
    if (Objects.isNull(txData)) {
      return null;
    }
    log.info("Tx data for txHash {} is {}", txHash, txData);
    Array array = cardanoService.decodeExtraData(txData);
    TransactionExtraData extraData = DataItemDecodeUtil.changeFromMaptoObject(
        (Map) array.getDataItems().get(1));
    log.info(array + "[constructionParse] Decoded");
    TransactionParsed result;
    NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(
        mempoolTransactionRequest.getNetworkIdentifier());
    result = ParseConstructionUtils.parseSignedTransaction(networkIdentifier,
        ((UnicodeString) array.getDataItems().get(0)).getString(), extraData);
    MempoolTransactionResponse mempoolTransactionResponse = MempoolTransactionResponse.builder()
        .transaction(new org.cardanofoundation.rosetta.api.model.Transaction(
            new TransactionIdentifier(txHash), result.getOperations())).build();
    return mempoolTransactionResponse;
  }

  @Override
  public Set<String> getAllTransactionsInMempool() {
    return redisTemplate.keys(Constants.REDIS_PREFIX_MEMPOOL + "*");
  }
}
