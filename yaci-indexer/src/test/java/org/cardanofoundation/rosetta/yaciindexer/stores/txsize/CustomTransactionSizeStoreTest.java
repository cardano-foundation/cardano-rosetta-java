package org.cardanofoundation.rosetta.yaciindexer.stores.txsize;

import com.bloxbean.cardano.yaci.store.events.TransactionEvent;
import org.assertj.core.api.Assertions;
import org.cardanofoundation.rosetta.yaciindexer.TestDataGenerator;
import org.cardanofoundation.rosetta.yaciindexer.service.TransactionScriptSizeCalculator;
import org.cardanofoundation.rosetta.yaciindexer.service.TransactionScriptSizeCalculatorImpl;
import org.cardanofoundation.rosetta.yaciindexer.service.TransactionSizeCalculatorImpl;
import org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model.TransactionSizeEntity;
import org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model.TransactionSizeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class CustomTransactionSizeStoreTest {

  private CustomTransactionSizeStore customTransactionSizeStore;

  @Mock
  private TransactionSizeRepository transactionSizeRepository;

  @Spy
  private TransactionSizeCalculatorImpl transactionSizeCalculator = new TransactionSizeCalculatorImpl();

  @Spy
  private TransactionScriptSizeCalculator transactionScriptSizeCalculator = new TransactionScriptSizeCalculatorImpl();

  @Captor
  private ArgumentCaptor<List<TransactionSizeEntity>> captor;

  @BeforeEach
  void setup() {
    customTransactionSizeStore = new CustomTransactionSizeStore(
            transactionSizeRepository, transactionSizeCalculator, transactionScriptSizeCalculator);
  }

  @Test
  void handleTransactionEventTest() {
    TransactionEvent transactionEvent = TestDataGenerator.newTransactionEvent();
    customTransactionSizeStore.handleTransactionEvent(transactionEvent);

    Mockito.verify(transactionSizeRepository).saveAll(captor.capture());

    List<TransactionSizeEntity> actual = captor.getValue();

    Assertions.assertThat(actual)
            .containsExactlyInAnyOrder(
                    new TransactionSizeEntity("txHash1", 70001L, 36, 144),
                    new TransactionSizeEntity("txHash2", 70002L, 36, 192),
                    new TransactionSizeEntity("txHash3", 70003L, 36, 240)
            );
  }

}
