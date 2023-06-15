package org.cardanofoundation.rosetta.api.service;

import java.util.List;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;

public interface MempoolMonitoringService {
  MempoolResponse getAllTransaction(NetworkRequest networkRequest);

  List<byte[]> getAllTransactionsInMempool();
  MempoolTransactionResponse getDetailTransaction(MempoolTransactionRequest mempoolTransactionRequest);
}
