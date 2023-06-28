package org.cardanofoundation.rosetta.api.service;

import java.util.Set;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;

public interface MempoolMonitoringService {
  MempoolResponse getAllTransaction(NetworkRequest networkRequest);

  Set<String> getAllTransactionsInMempool();
  MempoolTransactionResponse getDetailTransaction(MempoolTransactionRequest mempoolTransactionRequest);
}
