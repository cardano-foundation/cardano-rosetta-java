package org.cardanofoundation.rosetta.common.util;

import java.io.File;
import java.io.IOException;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.cert.*;
import com.bloxbean.cardano.client.transaction.spec.governance.DRepType;
import com.bloxbean.cardano.client.util.HexUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationMetadata;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ApiException;

import static com.bloxbean.cardano.client.transaction.spec.cert.CertificateType.VOTE_DELEG_CERT;
import static org.assertj.core.api.Assertions.assertThat;
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
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.PREPROD.getNetwork(), resultAccumulator, OperationType.INPUT.getValue());

    assertEquals(1, resultAccumulator.getTransactionInputs().size());
    assertEquals(operation.getCoinChange().getCoinIdentifier().getIdentifier(),
        resultAccumulator.getTransactionInputs().getFirst().getTransactionId() + ":" + resultAccumulator.getTransactionInputs().getFirst().getIndex());

    assertEquals(operation.getAmount().getValue(), resultAccumulator.getInputAmounts().getFirst().toString());
  }
  @Test
  void parseOutputOperationTest() throws IOException {
    Operation operation = getOperation("testdata/construction/Operations/outputOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.PREPROD.getNetwork(), resultAccumulator, OperationType.OUTPUT.getValue());

    assertEquals(1, resultAccumulator.getTransactionOutputs().size());
    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getTransactionOutputs().getFirst().getAddress());

    assertEquals(operation.getAmount().getValue(), resultAccumulator.getOutputAmounts().getFirst().toString());
  }

  @Test
  void stakeKeyRegistrationTest() throws IOException, CborSerializationException {
    Operation operation = getOperation(
        "testdata/construction/Operations/stakeKeyRegistrationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
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
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
        OperationType.STAKE_KEY_DEREGISTRATION.getValue());
    StakeDeregistration certificateDto = (StakeDeregistration) resultAccumulator.getCertificates().getFirst();
    assertEquals("82018200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb", certificateDto.getCborHex());

  }

  @Test
  void stakeDelegationOperationTest() throws IOException, CborSerializationException {
    Operation operation = getOperation(
        "testdata/construction/Operations/stakeDelegationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
        OperationType.STAKE_DELEGATION.getValue());

    StakeDelegation stakeDelegation = (StakeDelegation) resultAccumulator.getCertificates().getFirst();

    assertThat(stakeDelegation.getCborHex()).isEqualTo("83028200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb581c1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5");
    assertThat(stakeDelegation.getType()).isEqualTo(CertificateType.STAKE_DELEGATION);
    assertThat(stakeDelegation.getStakePoolId().getPoolKeyHash()).isEqualTo(HexUtil.decodeHexString("1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5"));

    assertThat(resultAccumulator.getAddresses().getFirst()).isEqualTo("stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5");
  }

  @Test
  void dRepDelegationKeyHashOperationTest() throws IOException, CborSerializationException {
    Operation operation = getOperation(
            "testdata/construction/Operations/drepDelegationOperation-keyhash.json");

    ProcessOperations resultAccumulator = new ProcessOperations();

    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
            OperationType.VOTE_DREP_DELEGATION.getValue());

    VoteDelegCert voteRegDelegCert = (VoteDelegCert) resultAccumulator.getCertificates().getFirst();

    assertEquals("83098200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb8200581c74984fae4ca1715fa1f8759f9d871015ac87f449a85dea6cf9956da1", voteRegDelegCert.getCborHex());
    assertEquals(VOTE_DELEG_CERT, voteRegDelegCert.getType());
    assertEquals(DRepType.ADDR_KEYHASH, voteRegDelegCert.getDrep().getType());
    assertEquals("74984fae4ca1715fa1f8759f9d871015ac87f449a85dea6cf9956da1", voteRegDelegCert.getDrep().getHash());
  }

  @Test
  void dRepDelegationScriptHashOperationTest() throws IOException, CborSerializationException {
    Operation operation = getOperation(
            "testdata/construction/Operations/drepDelegationOperation-scripthash.json");

    ProcessOperations resultAccumulator = new ProcessOperations();

    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
            OperationType.VOTE_DREP_DELEGATION.getValue());

    VoteDelegCert voteRegDelegCert = (VoteDelegCert) resultAccumulator.getCertificates().getFirst();

    assertEquals("83098200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb8201581c41868c2b4e5289022a3a1f6f47f86823bc605c609d2c47a2db58e04a", voteRegDelegCert.getCborHex());
    assertEquals(VOTE_DELEG_CERT, voteRegDelegCert.getType());
    assertEquals(DRepType.SCRIPTHASH, voteRegDelegCert.getDrep().getType());
    assertEquals("41868c2b4e5289022a3a1f6f47f86823bc605c609d2c47a2db58e04a", voteRegDelegCert.getDrep().getHash());
  }

  @Test
  void dRepDelegationAbstainOperationTest() throws IOException, CborSerializationException {
    Operation operation = getOperation(
            "testdata/construction/Operations/drepDelegationOperation-abstain.json");

    ProcessOperations resultAccumulator = new ProcessOperations();

    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
            OperationType.VOTE_DREP_DELEGATION.getValue());

    VoteDelegCert voteRegDelegCert = (VoteDelegCert) resultAccumulator.getCertificates().getFirst();

    assertEquals("83098200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb8102", voteRegDelegCert.getCborHex());
    assertEquals(VOTE_DELEG_CERT, voteRegDelegCert.getType());
    assertEquals(DRepType.ABSTAIN, voteRegDelegCert.getDrep().getType());
  }

  @Test
  void dRepDelegationNoConfidenceOperationTest() throws IOException, CborSerializationException {
    Operation operation = getOperation(
            "testdata/construction/Operations/drepDelegationOperation-noconfidence.json");

    ProcessOperations resultAccumulator = new ProcessOperations();

    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
            OperationType.VOTE_DREP_DELEGATION.getValue());

    VoteDelegCert voteRegDelegCert = (VoteDelegCert) resultAccumulator.getCertificates().getFirst();

    assertEquals("83098200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb8103", voteRegDelegCert.getCborHex());
    assertEquals(VOTE_DELEG_CERT, voteRegDelegCert.getType());
    assertEquals(DRepType.NO_CONFIDENCE, voteRegDelegCert.getDrep().getType());
  }

  @Test
  void withdrawalOperationTest() throws IOException {
    Operation operation = getOperation("testdata/construction/Operations/withdrawalOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator, OperationType.WITHDRAWAL.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
    assertEquals(operation.getAmount().getValue(), resultAccumulator.getWithdrawalAmounts().getFirst().toString());
  }

  @Test
  void poolRegistrationOperationTest() throws IOException {
    Operation operation = getOperation(
        "testdata/construction/Operations/poolRegistrationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
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
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
        OperationType.POOL_RETIREMENT.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
  }

  @Test
  void parseOperationEmptyAddressTest() {
    ProcessOperations resultAccumulator = new ProcessOperations();
    Operation operation = new Operation();
    ApiException exception = assertThrows(ApiException.class,
            () -> OperationParseUtil.parseOperation(operation, NetworkEnum.PREPROD.getNetwork(),
                    resultAccumulator, OperationType.OUTPUT.getValue()));

    assertEquals("Output has missing address field", exception.getError().getDetails().getMessage());
  }

  @Test
  void parsePoolRegistrationWithCertWOPullKeyHashTest() {
    ProcessOperations resultAccumulator = new ProcessOperations();
    Operation operation = new Operation();
    ApiException exception = assertThrows(ApiException.class,
            () -> OperationParseUtil.parseOperation(operation, NetworkEnum.PREPROD.getNetwork(),
                    resultAccumulator, OperationType.POOL_REGISTRATION_WITH_CERT.getValue()));

    assertEquals("Pool key hash is required to operate", exception.getError().getMessage());
  }

  @Test
  void parseVoteRegistrationWOMetadataTest() {
    ProcessOperations resultAccumulator = new ProcessOperations();
    Operation operation = new Operation();
    ApiException exception = assertThrows(ApiException.class,
            () -> OperationParseUtil.parseOperation(operation, NetworkEnum.PREPROD.getNetwork(),
                    resultAccumulator, OperationType.VOTE_REGISTRATION.getValue()));

    assertEquals("Missing vote registration metadata", exception.getError().getMessage());
  }

  @Test
  void parseOperationWOAddressStakingKey() {
    ProcessOperations resultAccumulator = new ProcessOperations();
    Operation operation = new Operation();
    ApiException exception = assertThrows(ApiException.class,
            () -> OperationParseUtil.parseOperation(operation, NetworkEnum.PREPROD.getNetwork(),
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
            () -> OperationParseUtil.parseOperation(operation, NetworkEnum.PREPROD.getNetwork(),
                    resultAccumulator, OperationType.POOL_REGISTRATION.getValue()));

    assertEquals("Pool registration parameters were expected", exception.getError().getMessage());
    assertEquals(4029, exception.getError().getCode());
  }

}
