package org.cardanofoundation.rosetta.api.search.service;

import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsRequest;
import org.springframework.data.domain.Page;

public interface SearchService {

  Page<BlockTransaction> searchTransaction(SearchTransactionsRequest searchTransactionsRequest,
                                           Long offset,
                                           Long limit);

}
