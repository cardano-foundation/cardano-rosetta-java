package org.cardanofoundation.rosetta.api.search.mapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.openapitools.client.model.BlockIdentifier;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsResponse;
import org.openapitools.client.model.Transaction;
import org.openapitools.client.model.TransactionMetadata;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperSetup;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockTransactionsToSearchTransactionsResponseTest extends BaseMapperSetup {

  @Autowired
  private SearchMapper my;

  @Test
  void mapToSearchResponse_test_Ok() {
    List<BlockTransaction> blockTransactions = List.of(BlockTransaction.builder()
            .blockIdentifier(BlockIdentifier.builder()
                .hash("hash")
                .index(1L)
                .build())
            .transaction(Transaction.builder()
                .metadata(TransactionMetadata.builder()
                    .size(10L)
                    .scriptSize(11L)
                    .build())
          .build())
        .build());

    SearchTransactionsResponse searchTransactionsResponse = my.mapToSearchTransactionsResponse(
        blockTransactions, 2L);

    assertEquals(1,searchTransactionsResponse.getTransactions().size());
    assertEquals(2L, searchTransactionsResponse.getNextOffset());
    assertEquals(1, searchTransactionsResponse.getTotalCount());
    assertEquals("hash", searchTransactionsResponse.getTransactions().getFirst().getBlockIdentifier().getHash());
    assertEquals(1L, searchTransactionsResponse.getTransactions().getFirst().getBlockIdentifier().getIndex());
  }

}
