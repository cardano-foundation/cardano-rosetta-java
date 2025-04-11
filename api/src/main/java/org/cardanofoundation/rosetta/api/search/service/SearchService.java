package org.cardanofoundation.rosetta.api.search.service;

import org.springframework.data.domain.Slice;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsRequest;

public interface SearchService {

  Slice<BlockTransaction> searchTransaction(SearchTransactionsRequest searchTransactionsRequest,
                                            Long offset,
                                            Long pageSize);

}
