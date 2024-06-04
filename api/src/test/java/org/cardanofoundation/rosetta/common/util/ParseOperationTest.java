package org.cardanofoundation.rosetta.common.util;

import java.io.File;
import java.io.IOException;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeDeregistration;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeRegistration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationMetadata;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("java:S5778")
class ParseOperationTest {

  private Operation getOperation(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    Operation operation = mapper.readValue(file, Operation.class);
    return operation;
  }

  @Test
  void parseInputOperationTest() throws IOException {
    Operation operation = getOperation("testdata/construction/Operations/inputOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_PREPROD_NETWORK, resultAccumulator, OperationType.INPUT.getValue());

    assertEquals(1, resultAccumulator.getTransactionInputs().size());
    assertEquals(operation.getCoinChange().getCoinIdentifier().getIdentifier(),
        resultAccumulator.getTransactionInputs().getFirst().getTransactionId() + ":" + resultAccumulator.getTransactionInputs().getFirst().getIndex());

    assertEquals(operation.getAmount().getValue(), resultAccumulator.getInputAmounts().getFirst().toString());
    System.out.println(operation);
  }
  @Test
  void parseOutputOperationTest() throws IOException {
    Operation operation = getOperation("testdata/construction/Operations/outputOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_PREPROD_NETWORK, resultAccumulator, OperationType.OUTPUT.getValue());

    assertEquals(1, resultAccumulator.getTransactionOutputs().size());
    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getTransactionOutputs().getFirst().getAddress());

    assertEquals(operation.getAmount().getValue(), resultAccumulator.getOutputAmounts().getFirst().toString());
  }

  @Test
  void stakeKeyRegistrationTest() throws IOException, CborSerializationException {
    Operation operation = getOperation(
        "testdata/construction/Operations/stakeKeyRegistrationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation,
        NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.STAKE_KEY_REGISTRATION.getValue());
    StakeRegistration stakeRegistration = (StakeRegistration) resultAccumulator.getCertificates()
        .getFirst();
    String cborHex = stakeRegistration.getStakeCredential().getCborHex();

    assertEquals("8200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb", cborHex);

  }

  @Test
  void stakeKeyDeregistrationTest() throws IOException, CborSerializationException {
    Operation operation = getOperation(
        "testdata/construction/Operations/stakeKeyDeregistrationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation,
        NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.STAKE_KEY_DEREGISTRATION.getValue());
    StakeDeregistration certificateDto = (StakeDeregistration) resultAccumulator.getCertificates().getFirst();
    assertEquals("82018200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb", certificateDto.getCborHex());

  }

  @Test
  void stakeDelegationOperationTest() throws IOException {
    Operation operation = getOperation(
        "testdata/construction/Operations/stakeDelegationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.STAKE_DELEGATION.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
  }

  @Test
  void withdrawalOperationTest() throws IOException {
    Operation operation = getOperation("testdata/construction/Operations/withdrawalOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator, OperationType.WITHDRAWAL.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
    assertEquals(operation.getAmount().getValue(), resultAccumulator.getWithdrawalAmounts().getFirst().toString());
  }

  @Test
  void poolRegistrationOperationTest() throws IOException {
    Operation operation = getOperation(
        "testdata/construction/Operations/poolRegistrationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.POOL_REGISTRATION.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().get(2));
    String poolOwner = operation.getMetadata().getPoolRegistrationParams().getPoolOwners().getFirst();
    assertEquals(poolOwner, resultAccumulator.getAddresses().getFirst());
  }

  @Test
  void poolRetirementOperationTest() throws IOException {
    Operation operation = getOperation(
        "testdata/construction/Operations/poolRetirementOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.POOL_RETIREMENT.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
  }

  @Test
  void parseOperationEmptyAddressTest() {
    ProcessOperations resultAccumulator = new ProcessOperations();
    Operation operation = new Operation();
    ApiException exception = assertThrows(ApiException.class,
            () -> OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_PREPROD_NETWORK,
                    resultAccumulator, OperationType.OUTPUT.getValue()));

    assertEquals("Output has missing address field", exception.getError().getDetails().getMessage());
  }

  @Test
  void parsePoolRegistrationWithCertWOPullKeyHashTest() {
    ProcessOperations resultAccumulator = new ProcessOperations();
    Operation operation = new Operation();
    ApiException exception = assertThrows(ApiException.class,
            () -> OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_PREPROD_NETWORK,
                    resultAccumulator, OperationType.POOL_REGISTRATION_WITH_CERT.getValue()));

    assertEquals("Pool key hash is required to operate", exception.getError().getMessage());
  }

  @Test
  void parseVoteRegistrationWOMetadataTest() {
    ProcessOperations resultAccumulator = new ProcessOperations();
    Operation operation = new Operation();
    ApiException exception = assertThrows(ApiException.class,
            () -> OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_PREPROD_NETWORK,
                    resultAccumulator, OperationType.VOTE_REGISTRATION.getValue()));

    assertEquals("Missing vote registration metadata", exception.getError().getMessage());
  }

  @Test
  void parseOperationWOAddressStakingKey() {
    ProcessOperations resultAccumulator = new ProcessOperations();
    Operation operation = new Operation();
    ApiException exception = assertThrows(ApiException.class,
            () -> OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_PREPROD_NETWORK,
                    resultAccumulator, OperationType.WITHDRAWAL.getValue()));

    assertEquals("Staking key is required for this type of address", exception.getError().getMessage());
    assertEquals(4018, exception.getError().getCode());
  }

  @Test
  void parsePoolRegistrationOperationWOParameters() {
    ProcessOperations resultAccumulator = new ProcessOperations();
    Operation operation = new Operation();
    operation.setMetadata(OperationMetadata
            .builder()
            .build());
    ApiException exception = assertThrows(ApiException.class,
            () -> OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_PREPROD_NETWORK,
                    resultAccumulator, OperationType.POOL_REGISTRATION.getValue()));

    assertEquals("Pool registration parameters were expected", exception.getError().getMessage());
    assertEquals(4029, exception.getError().getCode());
  }
}
