package org.cardanofoundation.rosetta.api.service.impl;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.config.CachedMemPool;
import org.cardanofoundation.rosetta.api.model.Transaction;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.TransactionParsed;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.MempoolMonitoringService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MempoolMonitoringServiceImpl implements MempoolMonitoringService {

  private final LocalTxMonitorClient localTxMonitorClient;
  final CardanoService cardanoService;
  final CachedMemPool cachedMemPool;
  @Override
  public MempoolResponse getAllTransaction(NetworkRequest networkRequest) {
    log.debug("[allTransaction] Request received: " + networkRequest);

    List<byte[]> txBytesList = localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono()
        .block();
    if (Objects.isNull(txBytesList)) {
      txBytesList = Collections.emptyList();
    }
    log.info("[allTransaction] Looking for all transaction in mempool" + txBytesList);
    List<TransactionIdentifier> transactionIdentifierList =
        txBytesList.stream()
            .map(txBytes -> {
              String txHash = TransactionUtil.getTxHash(txBytes);
              return new TransactionIdentifier(txHash);
            }).toList();
    MempoolResponse mempoolResponse = MempoolResponse.builder()
        .transactionIdentifierList(transactionIdentifierList)
        .build();
    log.debug("[allTransaction] About to return " + mempoolResponse);
    return mempoolResponse;
  }
  @Override
  public MempoolTransactionResponse getDetailTransaction(
      MempoolTransactionRequest mempoolTransactionRequest){

    log.debug("[detailTransaction] Request received: " + mempoolTransactionRequest);
    List<byte[]> txBytesList = localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono()
        .blockOptional().orElse(Collections.emptyList());
    MempoolTransactionResponse mempoolTransactionResponse = new MempoolTransactionResponse();

    for (byte[] txBytes : txBytesList) {
      String txHash = TransactionUtil.getTxHash(txBytes);
      if (txHash.equals(mempoolTransactionRequest.getTransactionIdentifier().getHash())) {
        log.info("Get information transaction with hash >> " + txHash);
        log.info("Operation information {}", cachedMemPool.getCachedMap().get(txHash).toString());
//        NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(mempoolTransactionRequest.getNetworkIdentifier());
//        Array array = decodeExtraData(txBytes);

//        TransactionExtraData extraData = cardanoService.changeFromMaptoObject((Map) array.getDataItems().get(1));
        TransactionParsed result = cachedMemPool.getCachedMap().get(txHash);
        mempoolTransactionResponse = MempoolTransactionResponse.builder().
            transaction(new Transaction(new TransactionIdentifier(txHash), result.getOperations()))
            .build();
      }
    }
    log.debug("[detailTransaction] Transaction detail to return " + mempoolTransactionResponse);
    return mempoolTransactionResponse;
  }

  public Array decodeExtraData(byte[] txBytes) {
    DataItem dataItem = com.bloxbean.cardano.client.common.cbor.CborSerializationUtil.deserialize(txBytes);
    return (Array) dataItem ;
  }


  @Override
  public List<byte[]> getAllTransactionsInMempool() {
    return localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono()
        .blockOptional()
        .orElse(Collections.emptyList());
  }
}
