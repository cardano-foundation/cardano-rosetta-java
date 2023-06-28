package org.cardanofoundation.rosetta.consumer.unit.service.impl.certificate;

import java.util.Collections;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.GenesisKeyDelegation;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.service.impl.certificate.GenesisKeyDelegationServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GenesisKeyDelegationServiceImplTest {

  @Test
  @DisplayName("No-op test")
  void noOpTest() {
    // This test is there just to add coverage
    GenesisKeyDelegationServiceImpl victim = new GenesisKeyDelegationServiceImpl();
    Assertions.assertDoesNotThrow(() -> victim.handle(
        Mockito.mock(AggregatedBlock.class),
        Mockito.mock(GenesisKeyDelegation.class),
        0,
        Mockito.mock(Tx.class),
        Mockito.mock(Redeemer.class),
        Collections.emptyMap()
    ));
  }
}
