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

public class parseOperationTest {


  private Operation getOperation(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    Operation operation = mapper.readValue(file, Operation.class);
    return operation;
  }

  @Test
  public void parseInputOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation("testdata/construction/Operations/inputOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_PREPROD_NETWORK, resultAccumulator, OperationType.INPUT.getValue());

    assertEquals(resultAccumulator.getTransactionInputs().size(), 1);
    assertEquals(operation.getCoinChange().getCoinIdentifier().getIdentifier(),
        resultAccumulator.getTransactionInputs().get(0).getTransactionId() + ":" + resultAccumulator.getTransactionInputs().get(0).getIndex());

    assertEquals(operation.getAmount().getValue(), resultAccumulator.getInputAmounts().get(0).toString());
    System.out.println(operation);
  }

  @Test
  public void parseOutputOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation("testdata/construction/Operations/outputOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_PREPROD_NETWORK, resultAccumulator, OperationType.OUTPUT.getValue());

    assertEquals(resultAccumulator.getTransactionOutputs().size(), 1);
    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getTransactionOutputs().get(0).getAddress());

    assertEquals(operation.getAmount().getValue(), resultAccumulator.getOutputAmounts().get(0).toString());
  }

  @Test
  public void stakeKeyRegistrationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation(
        "testdata/construction/Operations/stakeKeyRegistrationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation,
        NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.STAKE_KEY_REGISTRATION.getValue());
    StakeRegistration stakeRegistration = (StakeRegistration) resultAccumulator.getCertificates()
        .get(0);
    String cborHex = stakeRegistration.getStakeCredential().getCborHex();

    assertEquals("8200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb", cborHex);

  }

  @Test
  public void stakeKeyDeregistrationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation(
        "testdata/construction/Operations/stakeKeyDeregistrationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation,
        NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.STAKE_KEY_DEREGISTRATION.getValue());
    StakeDeregistration certificateDto = (StakeDeregistration) resultAccumulator.getCertificates().get(0);
    assertEquals("82018200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb", certificateDto.getCborHex());

  }

  @Test
  public void stakeDelegationOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation(
        "testdata/construction/Operations/stakeDelegationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.STAKE_DELEGATION.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().get(0));
  }

    @Test
  public void withdrawalOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation("testdata/construction/Operations/withdrawalOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator, OperationType.WITHDRAWAL.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().get(0));
    assertEquals(operation.getAmount().getValue(), resultAccumulator.getWithdrawalAmounts().get(0).toString());
  }

  @Test
  public void poolRegistrationOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation(
        "testdata/construction/Operations/poolRegistrationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.POOL_REGISTRATION.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().get(2));
    String poolOwner = operation.getMetadata().getPoolRegistrationParams().getPoolOwners().get(0);
    assertEquals(poolOwner, resultAccumulator.getAddresses().get(0));
  }

  @Test
  public void poolRetirementOperationTest()
      throws IOException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, CborSerializationException, InvalidKeyException {
    Operation operation = getOperation(
        "testdata/construction/Operations/poolRetirementOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkIdentifierType.CARDANO_MAINNET_NETWORK, resultAccumulator,
        OperationType.POOL_RETIREMENT.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().get(0));
  }

}
