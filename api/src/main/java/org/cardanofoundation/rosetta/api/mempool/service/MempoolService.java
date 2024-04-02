package org.cardanofoundation.rosetta.api.mempool.service;

import java.util.List;
import org.openapitools.client.model.Transaction;
import org.openapitools.client.model.TransactionIdentifier;

public interface MempoolService {

  List<TransactionIdentifier> getCurrentTransactionIdentifiers(String network);
}
