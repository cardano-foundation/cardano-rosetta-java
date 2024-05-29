package org.cardanofoundation.rosetta.yaciindexer.stores.txsize;

import java.util.ArrayList;

import org.assertj.core.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.yaciindexer.TestDataGenerator;
import org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model.TransactionSizeEntity;
import org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model.TransactionSizeRepository;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomTransactionSizeStoreTest {

  private CustomTransactionSizeStore customTransactionSizeStore;

  @Mock
  private TransactionSizeRepository transactionSizeRepository;

  @Captor
  private ArgumentCaptor<ArrayList<TransactionSizeEntity>> captor;

  @BeforeEach
  void setup() {
    when(transactionSizeRepository.saveAll(captor.capture()))
        .thenAnswer(i -> i.getArguments()[0]);
    customTransactionSizeStore = Mockito.spy(
        new CustomTransactionSizeStore(transactionSizeRepository));
  }

  @Test
  void handleTransactionEventTest() {
    // given
    var transactionEvent = TestDataGenerator.newTransactionEvent();
    // when
    customTransactionSizeStore.handleTransactionEvent(transactionEvent);
    // then
    ArrayList<TransactionSizeEntity> actual = captor.getValue();
    Assertions.assertThat(actual)
        .containsExactlyInAnyOrder(
            new TransactionSizeEntity("txHash1", 70001L, 482, 144),
            new TransactionSizeEntity("txHash2", 70002L, 642, 192),
            new TransactionSizeEntity("txHash3", 70003L, 800, 240)
        );
  }

}
