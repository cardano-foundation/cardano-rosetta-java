package org.cardanofoundation.rosetta.common.util;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeDeregistration;
import com.bloxbean.cardano.client.transaction.spec.cert.StakeRegistration;
import com.bloxbean.cardano.client.transaction.spec.cert.VoteDelegCert;
import com.bloxbean.cardano.client.transaction.spec.governance.DRepType;
import com.bloxbean.cardano.client.transaction.spec.governance.VoterType;
import com.bloxbean.cardano.client.util.HexUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.GovernancePoolVote;
import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationMetadata;

import java.io.File;
import java.io.IOException;

import static com.bloxbean.cardano.client.address.CredentialType.Key;
import static com.bloxbean.cardano.client.transaction.spec.cert.CertificateType.VOTE_DELEG_CERT;
import static com.bloxbean.cardano.client.transaction.spec.governance.Vote.*;
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
  void stakeDelegationOperationTest() throws IOException {
    Operation operation = getOperation(
        "testdata/construction/Operations/stakeDelegationOperation.json");
    ProcessOperations resultAccumulator = new ProcessOperations();
    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
        OperationType.STAKE_DELEGATION.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
  }

  @Test
  void dRepDelegationKeyHashOperationTest() throws IOException, CborSerializationException {
    Operation operation = getOperation(
            "testdata/construction/Operations/drepDelegationOperation-keyhash.json");

    ProcessOperations resultAccumulator = new ProcessOperations();

    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
            OperationType.VOTE_DREP_DELEGATION.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
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

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
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

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
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

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());
    VoteDelegCert voteRegDelegCert = (VoteDelegCert) resultAccumulator.getCertificates().getFirst();

    assertEquals("83098200581cbb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb8103", voteRegDelegCert.getCborHex());
    assertEquals(VOTE_DELEG_CERT, voteRegDelegCert.getType());
    assertEquals(DRepType.NO_CONFIDENCE, voteRegDelegCert.getDrep().getType());
  }

  @Test
  void govPoolVote_VotedYes() throws IOException {
    Operation operation = getOperation(
            "testdata/construction/Operations/govPoolVoteOperation-YES.json");

    ProcessOperations resultAccumulator = new ProcessOperations();

    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
            OperationType.POOL_GOVERNANCE_VOTE.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());

    GovernancePoolVote vote = resultAccumulator.getGovernancePoolVotes().getFirst();

    assertEquals("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc", vote.getGovActionId().getTransactionId());
    assertEquals(0, vote.getGovActionId().getGovActionIndex());

    assertEquals(Key, vote.getVoter().getCredential().getType());
    assertEquals("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368", HexUtil.encodeHexString(vote.getVoter().getCredential().getBytes()));
    assertEquals(VoterType.STAKING_POOL_KEY_HASH, vote.getVoter().getType());

    assertEquals("https://ipfs.io/ipfs/bafybeihkoviema7g3gxyt6la7vd5ho32ictqbilu3wnlo3rs7ewhnp7lly", vote.getVoteRationale().getAnchorUrl());
    assertEquals("b15f9c28d23e7ba4f06d13c9a84e5db09afc7e9b2d34f182eab61cf7a023d1c5", HexUtil.encodeHexString(vote.getVoteRationale().getAnchorDataHash()));

    assertEquals(YES, vote.getVote());
  }

  @Test
  void govPoolVote_VotedNo() throws IOException {
    Operation operation = getOperation(
            "testdata/construction/Operations/govPoolVoteOperation-NO.json");

    ProcessOperations resultAccumulator = new ProcessOperations();

    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
            OperationType.POOL_GOVERNANCE_VOTE.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());

    GovernancePoolVote vote = resultAccumulator.getGovernancePoolVotes().getFirst();

    assertEquals("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc", vote.getGovActionId().getTransactionId());
    assertEquals(1, vote.getGovActionId().getGovActionIndex());

    assertEquals(Key, vote.getVoter().getCredential().getType());
    assertEquals("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368", HexUtil.encodeHexString(vote.getVoter().getCredential().getBytes()));
    assertEquals(VoterType.STAKING_POOL_KEY_HASH, vote.getVoter().getType());

    assertEquals("https://ipfs.io/ipfs/bafybeihkoviema7g3gxyt6la7vd5ho32ictqbilu3wnlo3rs7ewhnp7lly", vote.getVoteRationale().getAnchorUrl());
    assertEquals("b15f9c28d23e7ba4f06d13c9a84e5db09afc7e9b2d34f182eab61cf7a023d1c5", HexUtil.encodeHexString(vote.getVoteRationale().getAnchorDataHash()));

    assertEquals(NO, vote.getVote());
  }

  @Test
  void govPoolVote_VotedAbstain_MissingVoteRationale() throws IOException {
    Operation operation = getOperation(
            "testdata/construction/Operations/govPoolVoteOperation-ABSTAIN.json");

    ProcessOperations resultAccumulator = new ProcessOperations();

    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
            OperationType.POOL_GOVERNANCE_VOTE.getValue());

    assertEquals(operation.getAccount().getAddress(), resultAccumulator.getAddresses().getFirst());

    GovernancePoolVote vote = resultAccumulator.getGovernancePoolVotes().getFirst();

    String address = resultAccumulator.getAddresses().getFirst(); // to ensure addresses are processed

    assertEquals("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368", address);
    assertEquals("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc", vote.getGovActionId().getTransactionId());
    assertEquals(2, vote.getGovActionId().getGovActionIndex());

    assertEquals(Key, vote.getVoter().getCredential().getType());
    assertEquals("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368", HexUtil.encodeHexString(vote.getVoter().getCredential().getBytes()));
    assertEquals(VoterType.STAKING_POOL_KEY_HASH, vote.getVoter().getType());

    assertEquals(ABSTAIN, vote.getVote());
  }

  @Test
  void govPoolVote_VotedAbstain_CheckPoolHashCheckError() throws IOException {
    Operation operation = getOperation(
            "testdata/construction/Operations/govPoolVoteOperation-ABSTAIN-PoolHashMismatch.json");

    ProcessOperations resultAccumulator = new ProcessOperations();

    ApiException exception = assertThrows(ApiException.class, () -> {
      OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
              OperationType.POOL_GOVERNANCE_VOTE.getValue());
    });

    assertThat(exception.getError().getCode()).isEqualTo(5047);
  }

  @Test
  void govPoolVote_VotedYes_Extras() throws IOException {
    Operation operation = getOperation(
            "testdata/construction/Operations/govPoolVoteOperation-EXTRAS-YES.json");

    ProcessOperations resultAccumulator = new ProcessOperations();

    resultAccumulator = OperationParseUtil.parseOperation(operation, NetworkEnum.MAINNET.getNetwork(), resultAccumulator,
            OperationType.POOL_GOVERNANCE_VOTE.getValue());

    GovernancePoolVote vote = resultAccumulator.getGovernancePoolVotes().getFirst();

    String address = resultAccumulator.getAddresses().getFirst(); // to ensure addresses are processed

    assertEquals("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368", address);
    assertEquals("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc", vote.getGovActionId().getTransactionId());
    assertEquals(2, vote.getGovActionId().getGovActionIndex());

    assertEquals(Key, vote.getVoter().getCredential().getType());
    assertEquals("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368", HexUtil.encodeHexString(vote.getVoter().getCredential().getBytes()));
    assertEquals(VoterType.STAKING_POOL_KEY_HASH, vote.getVoter().getType());

    assertEquals(ABSTAIN, vote.getVote());
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
