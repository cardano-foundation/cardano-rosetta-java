package org.cardanofoundation.rosetta.api.mempool.service;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.openapitools.client.model.TransactionIdentifier;

@Profile("mempool")
public interface MempoolService {

  List<TransactionIdentifier> getCurrentTransactionIdentifiers(String network);
}
