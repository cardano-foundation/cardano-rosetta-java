package org.cardanofoundation.rosetta.yaciindexer.stores.txsize;

import java.util.List;

import com.bloxbean.cardano.yaci.store.events.TransactionEvent;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.yaciindexer.TestDataGenerator;
import org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model.TransactionSizeEntity;
import org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model.TransactionSizeRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CustomTransactionSizeStoreTest {

  private CustomTransactionSizeStore customTransactionSizeStore;

  @Mock
  private TransactionSizeRepository transactionSizeRepository;

  @Captor
  private ArgumentCaptor<List<TransactionSizeEntity>> captor;

  @BeforeEach
  void setup() {
    customTransactionSizeStore = new CustomTransactionSizeStore(transactionSizeRepository);
  }

  @Test
  void givenFullTxCbor_shouldStoreLedgerSizeAndPlutusScriptSize() {
    TransactionEvent transactionEvent = TestDataGenerator.newTransactionEvent();

    customTransactionSizeStore.handleTransactionEvent(transactionEvent);

    Mockito.verify(transactionSizeRepository).saveAll(captor.capture());
    // size: 19-byte Alonzo+ tx CBOR minus the is_valid byte; scriptSize: Plutus V1 + V2 + V3 script bytes
    assertThat(captor.getValue())
            .containsExactlyInAnyOrder(
                    new TransactionSizeEntity("txHash1", 70001L, 18, 144),
                    new TransactionSizeEntity("txHash2", 70002L, 18, 192),
                    new TransactionSizeEntity("txHash3", 70003L, 18, 240)
            );
  }

  @Test
  void givenNoFullTxCbor_shouldSkipThatTransaction() {
    TransactionEvent transactionEvent = TestDataGenerator.newTransactionEvent();
    transactionEvent.getTransactions().get(1).setTxCbor(null);

    customTransactionSizeStore.handleTransactionEvent(transactionEvent);

    Mockito.verify(transactionSizeRepository).saveAll(captor.capture());
    assertThat(captor.getValue())
            .extracting(TransactionSizeEntity::getTxHash)
            .containsExactlyInAnyOrder("txHash1", "txHash3");
  }

}
