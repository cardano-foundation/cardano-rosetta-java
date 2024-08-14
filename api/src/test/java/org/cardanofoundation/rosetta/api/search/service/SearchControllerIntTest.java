package org.cardanofoundation.rosetta.api.search.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.CoinIdentifier;
import org.openapitools.client.model.SearchTransactionsRequest;
import org.openapitools.client.model.TransactionIdentifier;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

import static org.assertj.core.api.Assertions.assertThat;

class SearchControllerIntTest extends IntegrationTest {

  @Autowired
  private SearchService service;

  @Test
  void searchAddressTransactions() {
    SearchTransactionsRequest req = SearchTransactionsRequest.builder()
        .address(TestConstants.TEST_ACCOUNT_ADDRESS)
        .build();

    List<BlockTransaction> blockTransactions = service.searchTransaction(req, 0L, 5L);
    assertThat(blockTransactions.size() == 5);
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
    List<BlockTransaction> blockTransactions = service.searchTransaction(req, 0L, 10L);
    assertThat(blockTransactions.size() == 1);
    BlockTransaction tx = blockTransactions.getFirst();
    assertThat(tx.getBlockIdentifier().getHash().equals(stakeKeyDeregistration.blockHash()));
    assertThat(tx.getBlockIdentifier().getIndex() == stakeKeyDeregistration.blockNumber());
    assertThat(tx.getTransaction().getTransactionIdentifier().getHash().equals(stakeKeyDeregistration.txHash()));
    assertThat(tx.getTransaction().getOperations().size() == 4);
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

    List<BlockTransaction> blockTransactions = service.searchTransaction(req, 0L, 10L);
    assertThat(blockTransactions.size() == 1);
    String identifier = blockTransactions.getFirst().getTransaction().getOperations().getFirst()
        .getCoinChange().getCoinIdentifier().getIdentifier();
    req = SearchTransactionsRequest.builder()
        .coinIdentifier(CoinIdentifier.builder()
            .identifier(identifier)
            .build())
        .build();

    blockTransactions = service.searchTransaction(req, 0L, 10L);
    assertThat(blockTransactions.size() == 2);
  }

}
