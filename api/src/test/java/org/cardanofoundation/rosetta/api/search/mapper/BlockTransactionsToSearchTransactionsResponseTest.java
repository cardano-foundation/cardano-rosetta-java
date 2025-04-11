package org.cardanofoundation.rosetta.api.search.mapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.openapitools.client.model.*;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperSetup;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockTransactionsToSearchTransactionsResponseTest extends BaseMapperSetup {

  @Autowired
  private SearchMapper my;

  @Test
  void mapToSearchResponse_test_Ok() {
    List<BlockTransaction> blockTransactions = List.of(
            BlockTransaction.builder()
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

    SearchTransactionsResponse searchTransactionsResponse = my.mapToSearchTransactionsResponse(blockTransactions, 1L, 1);

    assertEquals(1, searchTransactionsResponse.getTransactions().size());
    assertEquals(1, searchTransactionsResponse.getNextOffset());
    assertEquals(1, searchTransactionsResponse.getTotalCount());

    assertEquals("hash", searchTransactionsResponse.getTransactions().getFirst().getBlockIdentifier().getHash());
    assertEquals(1L, searchTransactionsResponse.getTransactions().getFirst().getBlockIdentifier().getIndex());
  }

}
