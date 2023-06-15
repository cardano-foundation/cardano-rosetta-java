package org.cardanofoundation.rosetta.api.service.impl;

import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.service.MempoolMonitoringService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MempoolMonitoringServiceImpl implements MempoolMonitoringService {

  private final LocalTxMonitorClient localTxMonitorClient;


  @Override
  public MempoolResponse getAllTransaction(NetworkRequest networkRequest) {
    log.debug("[allTransaction] Request received: " + networkRequest);

    List<byte[]> txBytesList = getAllTransactionsInMempool();

    log.info("[allTransaction] Looking for all transaction in mempool" + txBytesList);
    log.info("[allTransaction] Looking for {} transaction in mempool", txBytesList.size());

    List<TransactionIdentifier>
        transactionIdentifierList = txBytesList.stream()
        .map(
            txBytes -> {
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
  public List<byte[]> getAllTransactionsInMempool() {
    return localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono()
        .blockOptional()
        .orElse(Collections.emptyList());
  }
}
