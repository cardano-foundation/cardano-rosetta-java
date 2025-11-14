package org.cardanofoundation.rosetta.common.mapper;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.Withdrawal;
import org.cardanofoundation.rosetta.api.construction.service.TransactionOperationParserImpl;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link TransactionOperationParserImpl}.
 * <p>
 * Tests are organized by the public methods they test:
 * <ul>
 *   <li>{@link GetSignerFromOperationTests} - Tests for signer extraction from operations</li>
 *   <li>{@link GetOperationsFromTransactionDataTests} - Tests for operation extraction from transaction data</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class TransactionOperationParserImplTest {

  TransactionOperationParserImpl parser = new TransactionOperationParserImpl();

  /**
   * Tests for {@link TransactionOperationParserImpl#getSignerFromOperation(com.bloxbean.cardano.client.common.model.Network, Operation)}.
   */
  @Nested
  class GetSignerFromOperationTests {

    /**
     * Tests for pool-related operations (registration, retirement, registration with certificate).
     */
    @Nested
    class PoolOperations {

      @Test
      void shouldExtractAllSignersFromPoolRegistrationOperation() {
        Operation operation = createTestPoolRegistrationOperation("addr1", "rewardAddress",
            List.of("poolOwner1", "poolOwner2"));

        List<String> poolSigners = parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

        assertThat(poolSigners)
            .hasSize(4)
            .contains("addr1", "rewardAddress", "poolOwner1", "poolOwner2");
      }

      @Test
      void shouldExtractPaymentAddressFromPoolRetirementOperation() {
        Operation operation = createTestPoolRegistrationOperation("addr1", "rewardAddress",
            List.of("poolOwner1", "poolOwner2"));
        operation.setType(OperationType.POOL_RETIREMENT.getValue());

        List<String> poolSigners = parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

        assertThat(poolSigners).hasSize(1).contains("addr1");
      }

      @Test
      void shouldExtractSignersFromPoolRegistrationCertificate() {
        Operation operation = createTestPoolRegistrationOperation("addr1", "", null);
        operation.setType(OperationType.POOL_REGISTRATION_WITH_CERT.getValue());
        operation.getMetadata().setPoolRegistrationCert(
            "8a03581c1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b558208dd154228946bd12967c12bedb1cb6038b78f8b84a1760b1a788fa72a4af3db01a004c4b401a002dc6c0d81e820101581de1bb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb81581c7a9a4d5a6ac7a9d8702818fa3ea533e56c4f1de16da611a730ee3f008184001820445820f5d9505820f5d9ea167fd2e0b19647f18dd1e0826f706f6f6c4d6574616461746155726c58209ac2217288d1ae0b4e15c41b58d3e05a13206fd9ab81cb15943e4174bf30c90b");

        List<String> poolSigners = parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

        assertThat(poolSigners)
            .hasSize(3)
            .contains("stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5", "addr1",
                "stake1u9af5n26dtr6nkrs9qv05049x0jkcncau9k6vyd8xrhr7qq8tez5p");
      }

      @Test
      void shouldHandleNullPaymentAddressInPoolRegistration() {
        Operation operation = createTestPoolRegistrationOperation(null, "rewardAddress",
            List.of("poolOwner1", "poolOwner2"));

        List<String> poolSigners = parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

        assertThat(poolSigners)
            .hasSize(3)
            .contains("rewardAddress", "poolOwner1", "poolOwner2");

        operation.setAccount(null);
        poolSigners = parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);
        assertThat(poolSigners)
            .hasSize(3)
            .contains("rewardAddress", "poolOwner1", "poolOwner2");

        operation.getMetadata().setPoolRegistrationParams(null);
        poolSigners = parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);
        assertThat(poolSigners).isEmpty();

        operation.setMetadata(null);
        poolSigners = parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);
        assertThat(poolSigners).isEmpty();
      }

      @SuppressWarnings("java:S5778")
      @Test
      void shouldThrowExceptionWhenPoolCertificateIsMissing() {
        Operation operation = createTestPoolRegistrationOperation("addr1", null, null);
        operation.setType(OperationType.POOL_REGISTRATION_WITH_CERT.getValue());
        operation.getMetadata().setPoolRegistrationCert(null);

        ApiException actualException = assertThrows(ApiException.class,
            () -> parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation));

        assertThat(actualException.getError().getMessage())
            .isEqualTo(RosettaErrorType.POOL_CERT_MISSING.getMessage());
        assertThat(actualException.getError().getCode())
            .isEqualTo(RosettaErrorType.POOL_CERT_MISSING.getCode());

        operation.setMetadata(null);
        actualException = assertThrows(ApiException.class,
            () -> parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation));

        assertThat(actualException.getError().getMessage())
            .isEqualTo(RosettaErrorType.POOL_CERT_MISSING.getMessage());
        assertThat(actualException.getError().getCode())
            .isEqualTo(RosettaErrorType.POOL_CERT_MISSING.getCode());
      }
    }

    /**
     * Tests for staking-related operations that use staking credentials.
     */
    @Nested
    class StakingOperations {

      @Test
      void shouldDeriveRewardAddressFromStakingCredential() {
        Operation operation = Operation
            .builder()
            .type("someType")
            .metadata(OperationMetadata.builder()
                .stakingCredential(PublicKey.builder()
                    .curveType(CurveType.EDWARDS25519)
                    .hexBytes("1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F")
                    .build())
                .build())
            .build();

        List<String> poolSigners = parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

        assertThat(poolSigners)
            .hasSize(1)
            .contains("stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5");
      }
    }

    /**
     * Tests for governance-related operations (Conway era).
     */
    @Nested
    class GovernanceOperations {

      @Test
      void shouldExtractSignerFromDRepVoteDelegation() {
        Operation operation = Operation.builder()
            .type(OperationType.VOTE_DREP_DELEGATION.getValue())
            .account(AccountIdentifier.builder()
                .address("stake_test1uql3k2h9kskfma8y53vth7wmptmlw8zxx67cx7c3pwj8sqs6zl0wr")
                .build())
            .metadata(OperationMetadata.builder()
                .stakingCredential(PublicKey.builder()
                    .hexBytes("fdbee86a702c49d23f45ab80e697b93ec48b744d5f413ef59c338c3f7b2d2de8")
                    .curveType(CurveType.EDWARDS25519)
                    .build())
                .drep(DRepParams.builder()
                    .id("f72050aa252ccc6bf75747184910c0b9386298167656f935d6b6c26a")
                    .type(DRepTypeParams.KEY_HASH)
                    .build())
                .build())
            .build();

        List<String> signers = parser.getSignerFromOperation(NetworkEnum.PREPROD.getNetwork(), operation);

        assertThat(signers)
            .hasSize(1)
            .contains("stake_test1uql3k2h9kskfma8y53vth7wmptmlw8zxx67cx7c3pwj8sqs6zl0wr");
      }

      @Test
      void shouldExtractSignerFromDRepVoteDelegationAbstain() {
        Operation operation = Operation.builder()
            .type(OperationType.VOTE_DREP_DELEGATION.getValue())
            .account(AccountIdentifier.builder()
                .address("stake_test1uqehkck0lajq8gr28t9uxnuvgcqrc6070x3k9r8048z8y5gssrtvn")
                .build())
            .metadata(OperationMetadata.builder()
                .stakingCredential(PublicKey.builder()
                    .hexBytes("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                    .curveType(CurveType.EDWARDS25519)
                    .build())
                .drep(DRepParams.builder()
                    .type(DRepTypeParams.ABSTAIN)
                    .build())
                .build())
            .build();

        List<String> signers = parser.getSignerFromOperation(NetworkEnum.PREPROD.getNetwork(), operation);

        assertThat(signers)
            .hasSize(1)
            .contains("stake_test1uqehkck0lajq8gr28t9uxnuvgcqrc6070x3k9r8048z8y5gssrtvn");
      }

      @Test
      void shouldExtractSignerFromDRepVoteDelegationNoConfidence() {
        Operation operation = Operation.builder()
            .type(OperationType.VOTE_DREP_DELEGATION.getValue())
            .account(AccountIdentifier.builder()
                .address("stake_test1uqehkck0lajq8gr28t9uxnuvgcqrc6070x3k9r8048z8y5gssrtvn")
                .build())
            .metadata(OperationMetadata.builder()
                .stakingCredential(PublicKey.builder()
                    .hexBytes("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                    .curveType(CurveType.EDWARDS25519)
                    .build())
                .drep(DRepParams.builder()
                    .type(DRepTypeParams.NO_CONFIDENCE)
                    .build())
                .build())
            .build();

        List<String> signers = parser.getSignerFromOperation(NetworkEnum.PREPROD.getNetwork(), operation);

        assertThat(signers)
            .hasSize(1)
            .contains("stake_test1uqehkck0lajq8gr28t9uxnuvgcqrc6070x3k9r8048z8y5gssrtvn");
      }

      @Test
      void shouldExtractSignerFromPoolGovernanceVote() {
        Operation operation = Operation.builder()
            .type(OperationType.POOL_GOVERNANCE_VOTE.getValue())
            .account(AccountIdentifier.builder()
                .address("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368")
                .build())
            .metadata(OperationMetadata.builder()
                .poolGovernanceVoteParams(PoolGovernanceVoteParams.builder()
                    .governanceActionHash("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc00")
                    .poolCredential(PublicKey.builder()
                        .hexBytes("60afbe982faaee34b02ad0e75cd50d5d7a734f5daaf7b67bc8c492eb5299af2b")
                        .curveType(CurveType.EDWARDS25519)
                        .build())
                    .vote(GovVoteParams.YES)
                    .build())
                .build())
            .build();

        List<String> signers = parser.getSignerFromOperation(NetworkEnum.PREPROD.getNetwork(), operation);

        assertThat(signers)
            .hasSize(1)
            .contains("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368");
      }

      @Test
      void shouldExtractSignerFromPoolGovernanceVoteWithRationale() {
        Operation operation = Operation.builder()
            .type(OperationType.POOL_GOVERNANCE_VOTE.getValue())
            .account(AccountIdentifier.builder()
                .address("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368")
                .build())
            .metadata(OperationMetadata.builder()
                .poolGovernanceVoteParams(PoolGovernanceVoteParams.builder()
                    .governanceActionHash("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc00")
                    .poolCredential(PublicKey.builder()
                        .hexBytes("60afbe982faaee34b02ad0e75cd50d5d7a734f5daaf7b67bc8c492eb5299af2b")
                        .curveType(CurveType.EDWARDS25519)
                        .build())
                    .vote(GovVoteParams.YES)
                    .voteRationale(GovVoteRationaleParams.builder()
                        .dataHash("c77f8427e2808cbd4c7093aa704fb0fcb48b2ab3bdd84fa7f4dec2eb7de344c9")
                        .url("ipfs://bafybeig7hluox6xefqdgmwcntvsguxcziw2oeogg2fbvygex2aj6qcfo64")
                        .build())
                    .build())
                .build())
            .build();

        List<String> signers = parser.getSignerFromOperation(NetworkEnum.PREPROD.getNetwork(), operation);

        assertThat(signers)
            .hasSize(1)
            .contains("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368");
      }

      @Test
      void shouldExtractSignerFromPoolGovernanceVoteNo() {
        Operation operation = Operation.builder()
            .type(OperationType.POOL_GOVERNANCE_VOTE.getValue())
            .account(AccountIdentifier.builder()
                .address("pool1test12345678abcdefghijklmnopqrstuvwxyz")
                .build())
            .metadata(OperationMetadata.builder()
                .poolGovernanceVoteParams(PoolGovernanceVoteParams.builder()
                    .governanceActionHash("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc00")
                    .poolCredential(PublicKey.builder()
                        .hexBytes("60afbe982faaee34b02ad0e75cd50d5d7a734f5daaf7b67bc8c492eb5299af2b")
                        .curveType(CurveType.EDWARDS25519)
                        .build())
                    .vote(GovVoteParams.NO)
                    .build())
                .build())
            .build();

        List<String> signers = parser.getSignerFromOperation(NetworkEnum.PREPROD.getNetwork(), operation);

        assertThat(signers)
            .hasSize(1)
            .contains("pool1test12345678abcdefghijklmnopqrstuvwxyz");
      }

      @Test
      void shouldExtractSignerFromPoolGovernanceVoteAbstain() {
        Operation operation = Operation.builder()
            .type(OperationType.POOL_GOVERNANCE_VOTE.getValue())
            .account(AccountIdentifier.builder()
                .address("pool1test12345678abcdefghijklmnopqrstuvwxyz")
                .build())
            .metadata(OperationMetadata.builder()
                .poolGovernanceVoteParams(PoolGovernanceVoteParams.builder()
                    .governanceActionHash("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc00")
                    .poolCredential(PublicKey.builder()
                        .hexBytes("60afbe982faaee34b02ad0e75cd50d5d7a734f5daaf7b67bc8c492eb5299af2b")
                        .curveType(CurveType.EDWARDS25519)
                        .build())
                    .vote(GovVoteParams.ABSTAIN)
                    .build())
                .build())
            .build();

        List<String> signers = parser.getSignerFromOperation(NetworkEnum.PREPROD.getNetwork(), operation);

        assertThat(signers)
            .hasSize(1)
            .contains("pool1test12345678abcdefghijklmnopqrstuvwxyz");
      }

      @Test
      void shouldHandleNullAccountInGovernanceOperations() {
        Operation operation = Operation.builder()
            .type(OperationType.VOTE_DREP_DELEGATION.getValue())
            .account(null)
            .metadata(OperationMetadata.builder()
                .stakingCredential(PublicKey.builder()
                    .hexBytes("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                    .curveType(CurveType.EDWARDS25519)
                    .build())
                .drep(DRepParams.builder()
                    .type(DRepTypeParams.ABSTAIN)
                    .build())
                .build())
            .build();

        List<String> signers = parser.getSignerFromOperation(NetworkEnum.PREPROD.getNetwork(), operation);

        assertThat(signers)
            .hasSize(1)
            .contains("stake_test1uza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6nuuef");
      }
    }

    /**
     * Tests for error handling and edge cases.
     */
    @Nested
    class ErrorHandling {

      @SuppressWarnings("java:S5778")
      @Test
      void shouldThrowExceptionWhenStakingKeyIsMissing() {
        Operation operation = Operation.builder().metadata(null).type("invalidType").build();

        ApiException actualException = assertThrows(ApiException.class,
            () -> parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation));

        assertThat(actualException.getError().getMessage())
            .isEqualTo(RosettaErrorType.STAKING_KEY_MISSING.getMessage());
        assertThat(actualException.getError().getCode())
            .isEqualTo(RosettaErrorType.STAKING_KEY_MISSING.getCode());
      }

      /**
       * Verifies that all pool operation types are handled correctly.
       * <p>
       * Note: The default case in getPoolSigners has defensive error message code that's currently
       * unreachable with production code, since POOL_OPERATIONS list and switch statement cases
       * are kept in sync. The error message "pool operation not supported, operation:{type}"
       * provides future-proofing if they ever become out of sync.
       * </p>
       */
      @Test
      void shouldHandleAllPoolOperationTypes() {
        // Test pool registration
        Operation poolRegistration = Operation.builder()
            .type(OperationType.POOL_REGISTRATION.getValue())
            .account(AccountIdentifier.builder().address("addr1").build())
            .metadata(OperationMetadata.builder()
                .poolRegistrationParams(PoolRegistrationParams.builder()
                    .rewardAddress("rewardAddress")
                    .poolOwners(List.of("owner1"))
                    .build())
                .build())
            .build();
        assertThat(parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), poolRegistration))
            .isNotEmpty();

        // Test pool retirement
        Operation poolRetirement = Operation.builder()
            .type(OperationType.POOL_RETIREMENT.getValue())
            .account(AccountIdentifier.builder().address("addr1").build())
            .build();
        assertThat(parser.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), poolRetirement))
            .hasSize(1)
            .contains("addr1");
      }
    }
  }

  /**
   * Tests for {@link OperationServiceImpl#getOperationsFromTransactionData(TransactionData, com.bloxbean.cardano.client.common.model.Network)}.
   * <p>
   * Tests are organized by operation type:
   * <ul>
   *   <li>{@link PoolOperations} - Pool registration and retirement operations</li>
   *   <li>{@link StakingOperations} - Stake key registration, deregistration, and delegation</li>
   *   <li>{@link WithdrawalOperations} - Staking reward withdrawal operations</li>
   *   <li>{@link GovernanceOperations} - DRep vote delegation and pool governance votes</li>
   *   <li>{@link InputOutputOperations} - Input and output processing edge cases</li>
   * </ul>
   * </p>
   */
  @Nested
  class GetOperationsFromTransactionDataTests {

    /**
     * Tests for pool operations extraction from transaction data.
     * <p>
     * Note: Pool operations are complex and tested more thoroughly in integration tests.
     * These tests verify basic extraction capability.
     * </p>
     */
    @Nested
    class PoolOperations {

      @Test
      void shouldHandleTransactionWithPoolOperations()
          throws CborException, CborDeserializationException, CborSerializationException {
        TransactionData transactionData = getPoolTransactionData1();
        transactionData.transactionBody().setInputs(List.of(new TransactionInput()));

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.MAINNET.getNetwork());

        // Verify we can process transactions with pool operations without errors
        assertThat(operations).isNotEmpty();
      }
    }

    /**
     * Tests for staking operations extraction from transaction data.
     */
    @Nested
    class StakingOperations {

      @Test
      void shouldExtractStakeKeyRegistrationFromTransactionData()
          throws CborException, CborDeserializationException, CborSerializationException {
        Operation stakeRegOp = Operation.builder()
            .type(OperationType.STAKE_KEY_REGISTRATION.getValue())
            .operationIdentifier(OperationIdentifier.builder().index(1L).build())
            .account(AccountIdentifier.builder()
                .address("stake_test1uza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6nuuef")
                .build())
            .metadata(OperationMetadata.builder()
                .stakingCredential(PublicKey.builder()
                    .hexBytes("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                    .curveType(CurveType.EDWARDS25519)
                    .build())
                .build())
            .build();

        TransactionExtraData extraData = new TransactionExtraData(List.of(stakeRegOp));

        com.bloxbean.cardano.client.transaction.spec.cert.StakeRegistration stakeRegCert =
            new com.bloxbean.cardano.client.transaction.spec.cert.StakeRegistration(
                com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential.fromKey(
                    com.bloxbean.cardano.client.util.HexUtil.decodeHexString("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                )
            );

        TransactionBody transactionBody = TransactionBody.builder()
            .inputs(List.of(new TransactionInput()))
            .certs(List.of(stakeRegCert))
            .build();
        TransactionData transactionData = new TransactionData(transactionBody, extraData);

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.PREPROD.getNetwork());

        assertThat(operations).hasSizeGreaterThanOrEqualTo(2);
        long stakeRegCount = operations.stream()
            .filter(op -> op.getType().equals(OperationType.STAKE_KEY_REGISTRATION.getValue()))
            .count();
        assertThat(stakeRegCount).isEqualTo(1);
      }

      @Test
      void shouldExtractStakeKeyDeregistrationFromTransactionData()
          throws CborException, CborDeserializationException, CborSerializationException {
        Operation stakeDeregOp = Operation.builder()
            .type(OperationType.STAKE_KEY_DEREGISTRATION.getValue())
            .operationIdentifier(OperationIdentifier.builder().index(1L).build())
            .account(AccountIdentifier.builder()
                .address("stake_test1uza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6nuuef")
                .build())
            .metadata(OperationMetadata.builder()
                .stakingCredential(PublicKey.builder()
                    .hexBytes("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                    .curveType(CurveType.EDWARDS25519)
                    .build())
                .build())
            .build();

        TransactionExtraData extraData = new TransactionExtraData(List.of(stakeDeregOp));

        com.bloxbean.cardano.client.transaction.spec.cert.StakeDeregistration stakeDeregCert =
            new com.bloxbean.cardano.client.transaction.spec.cert.StakeDeregistration(
                com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential.fromKey(
                    com.bloxbean.cardano.client.util.HexUtil.decodeHexString("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                )
            );

        TransactionBody transactionBody = TransactionBody.builder()
            .inputs(List.of(new TransactionInput()))
            .certs(List.of(stakeDeregCert))
            .build();
        TransactionData transactionData = new TransactionData(transactionBody, extraData);

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.PREPROD.getNetwork());

        assertThat(operations).hasSizeGreaterThanOrEqualTo(2);
        long stakeDeregCount = operations.stream()
            .filter(op -> op.getType().equals(OperationType.STAKE_KEY_DEREGISTRATION.getValue()))
            .count();
        assertThat(stakeDeregCount).isEqualTo(1);
      }

      @Test
      void shouldExtractStakeDelegationFromTransactionData()
          throws CborException, CborDeserializationException, CborSerializationException {
        String poolKeyHash = "1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b5";

        Operation stakeDelegOp = Operation.builder()
            .type(OperationType.STAKE_DELEGATION.getValue())
            .operationIdentifier(OperationIdentifier.builder().index(1L).build())
            .account(AccountIdentifier.builder()
                .address("stake_test1uza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6nuuef")
                .build())
            .metadata(OperationMetadata.builder()
                .stakingCredential(PublicKey.builder()
                    .hexBytes("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                    .curveType(CurveType.EDWARDS25519)
                    .build())
                .poolKeyHash(poolKeyHash)
                .build())
            .build();

        TransactionExtraData extraData = new TransactionExtraData(List.of(stakeDelegOp));

        com.bloxbean.cardano.client.transaction.spec.cert.StakeDelegation stakeDelegCert =
            new com.bloxbean.cardano.client.transaction.spec.cert.StakeDelegation(
                com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential.fromKey(
                    com.bloxbean.cardano.client.util.HexUtil.decodeHexString("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                ),
                new com.bloxbean.cardano.client.transaction.spec.cert.StakePoolId(
                    com.bloxbean.cardano.client.util.HexUtil.decodeHexString(poolKeyHash)
                )
            );

        TransactionBody transactionBody = TransactionBody.builder()
            .inputs(List.of(new TransactionInput()))
            .certs(List.of(stakeDelegCert))
            .build();
        TransactionData transactionData = new TransactionData(transactionBody, extraData);

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.PREPROD.getNetwork());

        assertThat(operations).hasSizeGreaterThanOrEqualTo(2);
        long stakeDelegCount = operations.stream()
            .filter(op -> op.getType().equals(OperationType.STAKE_DELEGATION.getValue()))
            .count();
        assertThat(stakeDelegCount).isEqualTo(1);
      }
    }

    /**
     * Tests for withdrawal operations extraction from transaction data.
     */
    @Nested
    class WithdrawalOperations {

      @Test
      void shouldExtractWithdrawalOperationsFromTransactionData()
          throws CborException, CborDeserializationException, CborSerializationException {
        TransactionData transactionData = getPoolTransactionData1();
        transactionData.transactionBody().setInputs(List.of(new TransactionInput()));
        transactionData.transactionBody().setWithdrawals(List.of(new Withdrawal()));
        Operation withdrawalOperation = transactionData.transactionExtraData().operations().getFirst();
        withdrawalOperation.setType(OperationType.WITHDRAWAL.getValue());
        withdrawalOperation.setMetadata(OperationMetadata.builder()
            .stakingCredential(
                  PublicKey.builder()
                    .curveType(CurveType.EDWARDS25519)
                    .hexBytes("1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F")
                    .build())
            .build());
        withdrawalOperation.setOperationIdentifier(OperationIdentifier.builder().index(22L).build());
        withdrawalOperation.setAmount(Amount.builder().value("value").build());

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.MAINNET.getNetwork());

        assertThat(operations).hasSize(2);
        assertThat(operations.get(1).getType()).isEqualTo(OperationType.WITHDRAWAL.getValue());
        assertThat(operations.get(1).getOperationIdentifier().getIndex()).isEqualTo(22L);
        assertThat(operations.get(1).getAmount().getValue()).isEqualTo("value");
        assertThat(operations.get(1).getMetadata().getStakingCredential().getHexBytes())
            .isEqualTo("1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F");
      }
    }

    /**
     * Tests for governance operations (Conway era) extraction from transaction data.
     * <p>
     * Covers both certificate-based (DRep vote delegation) and non-certificate
     * (pool governance votes) governance operations.
     * </p>
     */
    @Nested
    class GovernanceOperations {

      /**
       * Tests pool governance vote extraction (non-certificate governance operation).
       * <p>
       * Pool governance votes are voting procedures (NOT certificates) and should be
       * extracted by fillGovOperations() even when no certificates exist in the transaction.
       * </p>
       */
      @Test
      void shouldExtractPoolGovernanceVoteWithoutCertificates()
          throws CborException, CborDeserializationException, CborSerializationException {
        Operation poolVoteOperation = Operation.builder()
            .type(OperationType.POOL_GOVERNANCE_VOTE.getValue())
            .operationIdentifier(OperationIdentifier.builder().index(1L).build())
            .account(AccountIdentifier.builder()
                .address("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368")
                .build())
            .metadata(OperationMetadata.builder()
                .poolGovernanceVoteParams(PoolGovernanceVoteParams.builder()
                    .governanceActionHash("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc00")
                    .poolCredential(PublicKey.builder()
                        .hexBytes("60afbe982faaee34b02ad0e75cd50d5d7a734f5daaf7b67bc8c492eb5299af2b")
                        .curveType(CurveType.EDWARDS25519)
                        .build())
                    .vote(GovVoteParams.YES)
                    .build())
                .build())
            .build();

        TransactionExtraData extraData = new TransactionExtraData(List.of(poolVoteOperation));
        TransactionBody transactionBody = TransactionBody.builder()
            .inputs(List.of(new TransactionInput()))
            .build();  // No certificates - pool votes are voting procedures, not certificates
        TransactionData transactionData = new TransactionData(transactionBody, extraData);

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.PREPROD.getNetwork());

        assertThat(operations).hasSize(2); // 1 input + 1 governance operation
        assertThat(operations.get(1).getType()).isEqualTo(OperationType.POOL_GOVERNANCE_VOTE.getValue());
        assertThat(operations.get(1).getAccount().getAddress())
            .isEqualTo("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368");
        assertThat(operations.get(1).getMetadata().getPoolGovernanceVoteParams().getGovernanceActionHash())
            .isEqualTo("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc00");
        assertThat(operations.get(1).getMetadata().getPoolGovernanceVoteParams().getVote())
            .isEqualTo(GovVoteParams.YES);
      }

      @Test
      void shouldExtractPoolGovernanceVoteFromTransactionData()
          throws CborException, CborDeserializationException, CborSerializationException {
        Operation poolVoteOperation = Operation.builder()
            .type(OperationType.POOL_GOVERNANCE_VOTE.getValue())
            .operationIdentifier(OperationIdentifier.builder().index(1L).build())
            .account(AccountIdentifier.builder()
                .address("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368")
                .build())
            .metadata(OperationMetadata.builder()
                .poolGovernanceVoteParams(PoolGovernanceVoteParams.builder()
                    .governanceActionHash("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc00")
                    .poolCredential(PublicKey.builder()
                        .hexBytes("60afbe982faaee34b02ad0e75cd50d5d7a734f5daaf7b67bc8c492eb5299af2b")
                        .curveType(CurveType.EDWARDS25519)
                        .build())
                    .vote(GovVoteParams.YES)
                    .build())
                .build())
            .build();

        TransactionExtraData extraData = new TransactionExtraData(List.of(poolVoteOperation));
        TransactionBody transactionBody = TransactionBody.builder()
            .inputs(List.of(new TransactionInput()))
            .build();
        TransactionData transactionData = new TransactionData(transactionBody, extraData);

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.PREPROD.getNetwork());

        assertThat(operations).hasSize(2); // 1 input + 1 governance operation
        assertThat(operations.get(1).getType()).isEqualTo(OperationType.POOL_GOVERNANCE_VOTE.getValue());
        assertThat(operations.get(1).getAccount().getAddress())
            .isEqualTo("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368");
        assertThat(operations.get(1).getMetadata().getPoolGovernanceVoteParams().getGovernanceActionHash())
            .isEqualTo("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc00");
        assertThat(operations.get(1).getMetadata().getPoolGovernanceVoteParams().getVote())
            .isEqualTo(GovVoteParams.YES);
      }

      /**
       * Tests that only non-certificate governance operations are extracted when no certificates exist.
       * <p>
       * This test demonstrates the correct behavior after Bug #1 fix:
       * - Pool governance votes (voting procedures) are extracted
       * - DRep vote delegations (certificates) are NOT extracted when no certificates in transaction body
       * </p>
       */
      @Test
      void shouldExtractOnlyNonCertificateGovernanceOperationsWithoutCertificates()
          throws CborException, CborDeserializationException, CborSerializationException {
        Operation drepOperation = Operation.builder()
            .type(OperationType.VOTE_DREP_DELEGATION.getValue())
            .operationIdentifier(OperationIdentifier.builder().index(1L).build())
            .account(AccountIdentifier.builder()
                .address("stake_test1uql3k2h9kskfma8y53vth7wmptmlw8zxx67cx7c3pwj8sqs6zl0wr")
                .build())
            .metadata(OperationMetadata.builder()
                .stakingCredential(PublicKey.builder()
                    .hexBytes("fdbee86a702c49d23f45ab80e697b93ec48b744d5f413ef59c338c3f7b2d2de8")
                    .curveType(CurveType.EDWARDS25519)
                    .build())
                .drep(DRepParams.builder()
                    .type(DRepTypeParams.ABSTAIN)
                    .build())
                .build())
            .build();

        Operation poolVoteOperation = Operation.builder()
            .type(OperationType.POOL_GOVERNANCE_VOTE.getValue())
            .operationIdentifier(OperationIdentifier.builder().index(2L).build())
            .account(AccountIdentifier.builder()
                .address("6c518b4861bb88b1395ceb116342cecbcfb8736282655f9a61c4c368")
                .build())
            .metadata(OperationMetadata.builder()
                .poolGovernanceVoteParams(PoolGovernanceVoteParams.builder()
                    .governanceActionHash("40c2a42fe324759a640dcfddbc69ef2e3b7fe5a998af8d6660359772bf44c9dc00")
                    .poolCredential(PublicKey.builder()
                        .hexBytes("60afbe982faaee34b02ad0e75cd50d5d7a734f5daaf7b67bc8c492eb5299af2b")
                        .curveType(CurveType.EDWARDS25519)
                        .build())
                    .vote(GovVoteParams.NO)
                    .build())
                .build())
            .build();

        TransactionExtraData extraData = new TransactionExtraData(List.of(drepOperation, poolVoteOperation));
        TransactionBody transactionBody = TransactionBody.builder()
            .inputs(List.of(new TransactionInput()))
            .build();  // No certificates!
        TransactionData transactionData = new TransactionData(transactionBody, extraData);

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.PREPROD.getNetwork());

        // Only pool governance vote should be extracted (DRep is a certificate and requires certs in transaction body)
        assertThat(operations).hasSize(2); // 1 input + 1 pool governance vote (NOT DRep)
        assertThat(operations.get(1).getType()).isEqualTo(OperationType.POOL_GOVERNANCE_VOTE.getValue());

        // Verify DRep operation was NOT extracted
        long drepCount = operations.stream()
            .filter(op -> op.getType().equals(OperationType.VOTE_DREP_DELEGATION.getValue()))
            .count();
        assertThat(drepCount).isEqualTo(0);
      }

      /**
       * Tests that DRep vote delegation IS properly extracted when certificates exist.
       * <p>
       * DRep vote delegation is a Cardano certificate (VoteDelegCert) and should be processed
       * when the transaction body contains actual VoteDelegCert certificates.
       * </p>
       */
      @Test
      void shouldExtractDRepOperationWhenCertificateExistsInTransactionBody()
          throws CborException, CborDeserializationException, CborSerializationException {

        // Create DRep operation in extra data
        Operation drepOperation = Operation.builder()
            .type(OperationType.VOTE_DREP_DELEGATION.getValue())
            .operationIdentifier(OperationIdentifier.builder().index(1L).build())
            .account(AccountIdentifier.builder()
                .address("stake_test1uza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6nuuef")
                .build())
            .metadata(OperationMetadata.builder()
                .stakingCredential(PublicKey.builder()
                    .hexBytes("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                    .curveType(CurveType.EDWARDS25519)
                    .build())
                .drep(DRepParams.builder()
                    .type(DRepTypeParams.ABSTAIN)
                    .build())
                .build())
            .build();

        // Create actual VoteDelegCert certificate to add to transaction body
        com.bloxbean.cardano.client.transaction.spec.cert.VoteDelegCert voteDelegCert =
            new com.bloxbean.cardano.client.transaction.spec.cert.VoteDelegCert(
                com.bloxbean.cardano.client.transaction.spec.cert.StakeCredential.fromKey(
                    com.bloxbean.cardano.client.util.HexUtil.decodeHexString("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f")
                ),
                com.bloxbean.cardano.client.transaction.spec.governance.DRep.abstain()
            );

        TransactionExtraData extraData = new TransactionExtraData(List.of(drepOperation));
        TransactionBody transactionBody = TransactionBody.builder()
            .inputs(List.of(new TransactionInput()))
            .certs(List.of(voteDelegCert))  // Add actual certificate!
            .build();
        TransactionData transactionData = new TransactionData(transactionBody, extraData);

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.PREPROD.getNetwork());

        // DRep operation SHOULD be extracted because certificate exists in transaction body
        long drepOperationCount = operations.stream()
            .filter(op -> op.getType().equals(OperationType.VOTE_DREP_DELEGATION.getValue()))
            .count();

        assertThat(drepOperationCount)
            .as("DRep operation should be extracted exactly once when certificate exists")
            .isEqualTo(1);

        assertThat(operations).hasSize(2); // 1 input + 1 DRep delegation

        // Verify the extracted operation has correct data
        Operation extractedDRep = operations.stream()
            .filter(op -> op.getType().equals(OperationType.VOTE_DREP_DELEGATION.getValue()))
            .findFirst()
            .orElseThrow();

        assertThat(extractedDRep.getAccount().getAddress())
            .isEqualTo("stake_test1uza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6nuuef");
        assertThat(extractedDRep.getMetadata().getDrep().getType())
            .isEqualTo(DRepTypeParams.ABSTAIN);
        assertThat(extractedDRep.getMetadata().getStakingCredential().getHexBytes())
            .isEqualToIgnoringCase("1b400d60aaf34eaf6dcbab9bba46001a23497886cf11066f7846933d30e5ad3f");
      }

      @Test
      void shouldHandleTransactionWithNoGovernanceOperations()
          throws CborException, CborDeserializationException, CborSerializationException {
        TransactionData transactionData = getPoolTransactionData1();
        transactionData.transactionBody().setInputs(List.of(new TransactionInput()));

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.MAINNET.getNetwork());

        assertThat(operations).hasSize(1); // Only input operation
        assertThat(operations).noneMatch(op ->
            op.getType().equals(OperationType.VOTE_DREP_DELEGATION.getValue()) ||
            op.getType().equals(OperationType.POOL_GOVERNANCE_VOTE.getValue())
        );
      }
    }

    /**
     * Tests for input and output processing edge cases.
     */
    @Nested
    class InputOutputOperations {

      /**
       * BUG TEST: This test exposes Bug #2 - IndexOutOfBoundsException when
       * extraDataInputOperations.size() < inputs.size()
       */
      @Test
      void shouldHandleMismatchBetweenExtraDataInputsAndTransactionInputs()
          throws CborException, CborDeserializationException, CborSerializationException {
        // Create 2 input operations in extraData
        Operation input1 = Operation.builder()
            .type(OperationType.INPUT.getValue())
            .operationIdentifier(OperationIdentifier.builder().index(0L).build())
            .account(AccountIdentifier.builder().address("addr1").build())
            .amount(Amount.builder().value("-1000000").build())
            .build();

        Operation input2 = Operation.builder()
            .type(OperationType.INPUT.getValue())
            .operationIdentifier(OperationIdentifier.builder().index(1L).build())
            .account(AccountIdentifier.builder().address("addr2").build())
            .amount(Amount.builder().value("-2000000").build())
            .build();

        TransactionExtraData extraData = new TransactionExtraData(List.of(input1, input2));

        // Create 5 inputs in transaction body (more than extraData)
        TransactionBody transactionBody = TransactionBody.builder()
            .inputs(List.of(
                new TransactionInput(),
                new TransactionInput(),
                new TransactionInput(),
                new TransactionInput(),
                new TransactionInput()
            ))
            .build();

        TransactionData transactionData = new TransactionData(transactionBody, extraData);

        // BUG: This will throw IndexOutOfBoundsException
        // When i=2, it tries to access extraDataInputOperations.get(2) but size is only 2
        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.MAINNET.getNetwork());

        // Should have 5 input operations (fallback to parsing from transactionBody when extraData insufficient)
        assertThat(operations).hasSizeGreaterThanOrEqualTo(5);
      }

      @Test
      void shouldHandleEmptyInputsList()
          throws CborException, CborDeserializationException, CborSerializationException {
        TransactionExtraData extraData = new TransactionExtraData(List.of());
        TransactionBody transactionBody = TransactionBody.builder()
            .inputs(List.of())
            .outputs(List.of())
            .build();
        TransactionData transactionData = new TransactionData(transactionBody, extraData);

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.MAINNET.getNetwork());

        assertThat(operations).isEmpty();
      }

      @Test
      void shouldHandleNullOutputsList()
          throws CborException, CborDeserializationException, CborSerializationException {
        TransactionExtraData extraData = new TransactionExtraData(List.of());
        TransactionBody transactionBody = TransactionBody.builder()
            .inputs(List.of())
            .outputs(null)
            .build();
        TransactionData transactionData = new TransactionData(transactionBody, extraData);

        List<Operation> operations = parser
            .getOperationsFromTransactionData(transactionData, NetworkEnum.MAINNET.getNetwork());

        assertThat(operations).isEmpty();
      }
    }
  }

  // ========== Helper Methods ==========

  private static Operation createTestPoolRegistrationOperation(String accountAddress, String rewardAddress,
                                                               List<String> poolOwners) {
    return Operation.builder()
        .type(OperationType.POOL_REGISTRATION.getValue())
        .account(AccountIdentifier.builder()
            .address(accountAddress)
            .build())
        .metadata(OperationMetadata.builder()
            .poolRegistrationParams(PoolRegistrationParams.builder()
                .rewardAddress(rewardAddress)
                .poolOwners(poolOwners)
                .build())
            .build())
        .build();
  }

  private static TransactionData getPoolTransactionData1() {
    Operation operation1 = createTestPoolRegistrationOperation("addr1", "rewardAddress", List.of("poolOwner1", "poolOwner2"));
    Operation operation2 = createTestPoolRegistrationOperation("addr2", "rewardAddress", List.of("poolOwner3", "poolOwner4"));
    TransactionExtraData transactionExtraData = new TransactionExtraData(List.of(operation1, operation2));

    return new TransactionData(
        TransactionBody.builder().build(),
            transactionExtraData
    );
  }

}
