package org.cardanofoundation.rosetta.api.service;

import java.util.List;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;

public interface MempoolMonitoringService {
  MempoolResponse getAllTransaction(NetworkRequest networkRequest);

  List<byte[]> getAllTransactionsInMempool();
}
