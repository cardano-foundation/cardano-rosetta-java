package org.cardanofoundation.rosetta.api.search.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.SearchTransactionsRequest;
import org.openapitools.client.model.TransactionIdentifier;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchControllerIntTest extends IntegrationTest {

  @Autowired
  private SearchService service;

  @Test
  void searchAddressTransactions() {
    SearchTransactionsRequest req = SearchTransactionsRequest.builder()
        .address(TestConstants.TEST_ACCOUNT_ADDRESS)
        .build();

    Slice<BlockTransaction> blockTransactions = service.searchTransaction(req, 0L, 5L);
    assertEquals(5, blockTransactions.getNumberOfElements());
  }

  @Test
  void searchByTxHash() {
    TransactionBlockDetails stakeKeyDeregistration = generatedDataMap.get(
        "stake_key_deregistration");
    SearchTransactionsRequest req = SearchTransactionsRequest.builder()
        .transactionIdentifier(TransactionIdentifier.builder()
            .hash(stakeKeyDeregistration.txHash())
            .build())
        .build();

    Slice<BlockTransaction> blockTransactions = service.searchTransaction(req, 0L, 10L);
    assertEquals(1, blockTransactions.getNumberOfElements());
    BlockTransaction tx = blockTransactions.getContent().getFirst();
    assertEquals(stakeKeyDeregistration.blockHash(), tx.getBlockIdentifier().getHash());
    assertEquals(stakeKeyDeregistration.blockNumber(), tx.getBlockIdentifier().getIndex());
    assertEquals(stakeKeyDeregistration.txHash(), tx.getTransaction().getTransactionIdentifier().getHash());
    assertEquals(4, tx.getTransaction().getOperations().size());
  }

  @Test
  void searchByUtxo() {
    TransactionBlockDetails txDetails = generatedDataMap.get(
        "simple_transaction");
    SearchTransactionsRequest req = SearchTransactionsRequest.builder()
        .transactionIdentifier(TransactionIdentifier.builder()
            .hash(txDetails.txHash())
            .build())
        .build();

    Slice<BlockTransaction> blockTransactions = service.searchTransaction(req, 0L, 10L);
    assertEquals(1, blockTransactions.getNumberOfElements());
    String identifier = blockTransactions.getContent().getFirst().getTransaction().getOperations().getFirst()
        .getCoinChange().getCoinIdentifier().getIdentifier();
    req = SearchTransactionsRequest.builder()
        .coinIdentifier(CoinIdentifier.builder()
            .identifier(identifier)
            .build())
        .build();

    blockTransactions = service.searchTransaction(req, 0L, 10L);
    assertEquals(2, blockTransactions.getNumberOfElements());
  }

}
