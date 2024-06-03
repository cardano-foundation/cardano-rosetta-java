package org.cardanofoundation.rosetta.common.mapper;

import java.util.List;

import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.Operation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.construction.service.OperationService;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {

  OperationService operationService = new OperationService();

  @Test
  void getPoolSignersFromOperationWOPoolSigners() {
    //given
    Operation operation = Operation
            .builder()
            .type("poolRegistration")
            .build();
    //when
    List<String> poolSigners = operationService.getSignerFromOperation(NetworkIdentifierType.CARDANO_MAINNET_NETWORK, operation);
    //then
    Assertions.assertEquals(0, poolSigners.size());
  }
}
