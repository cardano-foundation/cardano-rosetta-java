package org.cardanofoundation.rosetta.common.util;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeDeregistration;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeRegistration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.Operation;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParseOperationTest {


  private Operation getOperation(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    Operation operation = mapper.readValue(file, Operation.class);
    return operation;
  }

  @Test
  void parseInputOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
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
  void parseOutputOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation("testdata/construction/Operations/outputOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_PREPROD_NETWORK, resultAccumulator, OperationType.OUTPUT.getValue());

    assertEquals(1, resultAccumulator.getTransactionOutputs().size());
    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getTransactionOutputs().getFirst().getAddress());

    assertEquals(operation.getAmount().getValue(), resultAccumulator.getOutputAmounts().getFirst().toString());
  }

  @Test
  void stakeKeyRegistrationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
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
  void stakeKeyDeregistrationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
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
  void stakeDelegationOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation(
        "testdata/construction/Operations/stakeDelegationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.STAKE_DELEGATION.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
  }

  @Test
  void withdrawalOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation("testdata/construction/Operations/withdrawalOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator, OperationType.WITHDRAWAL.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
    assertEquals(operation.getAmount().getValue(), resultAccumulator.getWithdrawalAmounts().getFirst().toString());
  }

  @Test
  void poolRegistrationOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
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
  void poolRetirementOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation(
        "testdata/construction/Operations/poolRetirementOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.POOL_RETIREMENT.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
  }

}
