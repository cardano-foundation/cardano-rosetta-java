package org.cardanofoundation.rosetta.api.search.service;

import java.util.List;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsRequest;

public interface SearchService {

  List<BlockTransaction> searchTransaction(SearchTransactionsRequest searchTransactionsRequest, Long offset, Long pageSize);
}
