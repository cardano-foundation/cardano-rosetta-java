package org.cardanofoundation.rosetta.common.mapper;

import java.util.List;

import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.Operation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;

import static org.cardanofoundation.rosetta.common.mapper.TransactionDataToOperations.*;

@ExtendWith(MockitoExtension.class)
class TransactionDataToOperationsTest {

  @Test
  void getPoolSignersFromOperationWOPoolSigners() {
    //given
    Operation operation = Operation
            .builder()
            .type("poolRegistration")
            .build();
    //when
    List<String> poolSigners = getPoolSigners(NetworkIdentifierType.CARDANO_MAINNET_NETWORK, operation);
    //then
    Assertions.assertEquals(0, poolSigners.size());
  }
}
