package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;

public interface MempoolMonitoringService {
  MempoolResponse getAllTransaction(NetworkRequest networkRequest);

}
