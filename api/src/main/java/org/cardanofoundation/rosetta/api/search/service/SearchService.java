package org.cardanofoundation.rosetta.api.search.service;

import org.springframework.data.domain.Page;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsRequest;

public interface SearchService {

  Page<BlockTransaction> searchTransaction(SearchTransactionsRequest searchTransactionsRequest,
                                           Long offset,
                                           Long limit);

}
