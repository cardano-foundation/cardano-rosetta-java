package org.cardanofoundation.rosetta.api.service.impl;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.TransactionExtraData;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.MempoolMonitoringService;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MempoolMonitoringServiceImpl implements MempoolMonitoringService {

  private final LocalTxMonitorClient localTxMonitorClient;
  final CardanoService cardanoService;
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
  public MempoolTransactionResponse getDetailTransaction(MempoolTransactionRequest mempoolTransactionRequest) throws CborException, CborDeserializationException, UnknownHostException, AddressExcepion, CborSerializationException, JsonProcessingException {

    log.debug("[detailTransaction] Request received: " + mempoolTransactionRequest);
    List<byte[]> txBytesList = getAllTransactionsInMempool();
    MempoolTransactionResponse mempoolTransactionResponse = new MempoolTransactionResponse();

    for (byte[] txBytes : txBytesList) {
      String txHash = TransactionUtil.getTxHash(txBytes);
      if (txHash.equals(mempoolTransactionRequest.getTransactionIdentifier().getHash())) {
        log.info("Get information transaction with hash >> " + txHash);
        NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(
                mempoolTransactionRequest.getNetworkIdentifier()
        );
        List<DataItem> dataItemList = CborDecoder.decode(txBytes);
        Array array = (Array) dataItemList.get(0);
        if (dataItemList.size() >= 2 && array.getDataItems().size() == 3) {
          array.add(dataItemList.get(1));
        }
        Transaction parsed = Transaction.deserialize(CborSerializationUtil.serialize(array));
        List<Operation> operations = cardanoService.convert(parsed.getBody(),
                new TransactionExtraData(new ArrayList<>(),null) ,
                networkIdentifier.getValue());
        mempoolTransactionResponse = MempoolTransactionResponse.builder().
                transaction(
                        new org.cardanofoundation.rosetta.api.model.Transaction(
                                new TransactionIdentifier(txHash),
                                operations)
                )
                .build();
      }
    }
    log.debug("[detailTransaction] Transaction detail to return " + mempoolTransactionResponse);
    return mempoolTransactionResponse;
  }

  @Override
  public List<byte[]> getAllTransactionsInMempool() {
    return localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono()
        .blockOptional()
        .orElse(Collections.emptyList());
  }
}
