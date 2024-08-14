package org.cardanofoundation.rosetta.api.search.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.BlockIdentifier;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsResponse;
import org.openapitools.client.model.Transaction;
import org.openapitools.client.model.TransactionMetadata;
import org.springframework.beans.factory.annotation.Autowired;

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

    assertThat(searchTransactionsResponse.getTransactions().size() == 1);
    assertThat(searchTransactionsResponse.getNextOffset() == 2L);
    assertThat(searchTransactionsResponse.getTotalCount() == 1);
    assertThat(searchTransactionsResponse.getTransactions().get(0).getBlockIdentifier().getHash().equals("hash"));
    assertThat(searchTransactionsResponse.getTransactions().get(0).getBlockIdentifier().getIndex().equals(1L));
  }

}
