package org.cardanofoundation.rosetta.common.mapper;

import java.util.List;

import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.Operation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.construction.service.OperationService;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {

  OperationService operationService = new OperationService();

  @Test
  void getPoolSignersFromOperationWOPoolSigners() {
    Operation operation = Operation
            .builder()
            .type(OperationType.POOL_REGISTRATION.getValue())
            .build();

    List<String> poolSigners = operationService
        .getSignerFromOperation(NetworkIdentifierType.CARDANO_MAINNET_NETWORK, operation);

    assertEquals(0, poolSigners.size());
  }

  @Test
  void getPoolSignersFromOperationWithVoteSigners() {
    Operation operation = Operation
            .builder()
            .type(OperationType.VOTE_REGISTRATION.getValue())
            .build();

    List<String> poolSigners = operationService
        .getSignerFromOperation(NetworkIdentifierType.CARDANO_MAINNET_NETWORK, operation);

    assertEquals(0, poolSigners.size());
  }
}
