package org.cardanofoundation.rosetta.common.mapper;

import java.util.List;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.Withdrawal;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.construction.service.OperationService;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {

  OperationService operationService = new OperationService();

  @Test
  void getSignerFromOperation_poolOperationType_test() {
    Operation operation = creteTestPoolRegistrationOperation("addr1", "rewardAddress",
        List.of("poolOwner1", "poolOwner2"));

    List<String> poolSigners = operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

    assertThat(poolSigners)
        .hasSize(4)
        .contains("addr1", "rewardAddress", "poolOwner1", "poolOwner2");
  }

  @Test
  void getSignerFromOperation_poolOperationRetirementType_test() {
    Operation operation = creteTestPoolRegistrationOperation("addr1", "rewardAddress",
        List.of("poolOwner1", "poolOwner2"));
    operation.setType(OperationType.POOL_RETIREMENT.getValue());

    List<String> poolSigners = operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

    assertThat(poolSigners).hasSize(1).contains("addr1");
  }

  @Test
  void getSignerFromOperation_poolOperationTypeWithCertificate_test() {
    Operation operation = creteTestPoolRegistrationOperation("addr1", "", null);
    operation.setType(OperationType.POOL_REGISTRATION_WITH_CERT.getValue());
    operation.getMetadata().setPoolRegistrationCert(
        "8a03581c1b268f4cba3faa7e36d8a0cc4adca2096fb856119412ee7330f692b558208dd154228946bd12967c12bedb1cb6038b78f8b84a1760b1a788fa72a4af3db01a004c4b401a002dc6c0d81e820101581de1bb40f1a647bc88c1bd6b738db8eb66357d926474ea5ffd6baa76c9fb81581c7a9a4d5a6ac7a9d8702818fa3ea533e56c4f1de16da611a730ee3f008184001820445820f5d9505820f5d9ea167fd2e0b19647f18dd1e0826f706f6f6c4d6574616461746155726c58209ac2217288d1ae0b4e15c41b58d3e05a13206fd9ab81cb15943e4174bf30c90b");

    List<String> poolSigners = operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

    assertThat(poolSigners)
        .hasSize(3)
        .contains("stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5", "addr1",
            "stake1u9af5n26dtr6nkrs9qv05049x0jkcncau9k6vyd8xrhr7qq8tez5p");
  }

  @SuppressWarnings("java:S5778")
  @Test
  void getSignerFromOperation_poolOperationTypeWithCertificateNullable_test() {
    Operation operation = creteTestPoolRegistrationOperation("addr1", null, null);
    operation.setType(OperationType.POOL_REGISTRATION_WITH_CERT.getValue());
    operation.getMetadata().setPoolRegistrationCert(null);

    ApiException actualException = assertThrows(ApiException.class,
        () -> operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation));

    assertThat(actualException.getError().getMessage())
        .isEqualTo(RosettaErrorType.POOL_CERT_MISSING.getMessage());
    assertThat(actualException.getError().getCode())
        .isEqualTo(RosettaErrorType.POOL_CERT_MISSING.getCode());

    operation.setMetadata(null);
    actualException = assertThrows(ApiException.class,
        () -> operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation));

    assertThat(actualException.getError().getMessage())
        .isEqualTo(RosettaErrorType.POOL_CERT_MISSING.getMessage());
    assertThat(actualException.getError().getCode())
        .isEqualTo(RosettaErrorType.POOL_CERT_MISSING.getCode());
  }

  @Test
  void getSignerFromOperation_poolOperationTypeNullable_test() {
    Operation operation = creteTestPoolRegistrationOperation(null, "rewardAddress",
        List.of("poolOwner1", "poolOwner2"));

    List<String> poolSigners = operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

    assertThat(poolSigners)
        .hasSize(3)
        .contains("rewardAddress", "poolOwner1", "poolOwner2");

    operation.setAccount(null);
    poolSigners = operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);
    assertThat(poolSigners)
        .hasSize(3)
        .contains("rewardAddress", "poolOwner1", "poolOwner2");

    operation.getMetadata().setPoolRegistrationParams(null);
    poolSigners = operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);
    assertThat(poolSigners).isEmpty();

    operation.setMetadata(null);
    poolSigners = operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);
    assertThat(poolSigners).isEmpty();
  }

  @Test
  void getSignerFromOperation_voteRegistrationType_test() {
    Operation operation = Operation
        .builder()
        .type(OperationType.VOTE_REGISTRATION.getValue())
        .build();

    List<String> poolSigners = operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

    assertThat(poolSigners).isEmpty();
  }

  @Test
  void getSignerFromOperation_stakingCredentials_test() {
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

    List<String> poolSigners = operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation);

    assertThat(poolSigners)
        .hasSize(1)
        .contains("stake1uxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7caek7a5");
  }

  @SuppressWarnings("java:S5778")
  @Test
  void getSignerFromOperation_negative_test() {
    Operation operation = Operation.builder().metadata(null).type("invalidType").build();

    ApiException actualException = assertThrows(ApiException.class,
        () -> operationService.getSignerFromOperation(NetworkEnum.MAINNET.getNetwork(), operation));

    assertThat(actualException.getError().getMessage())
        .isEqualTo(RosettaErrorType.STAKING_KEY_MISSING.getMessage());
    assertThat(actualException.getError().getCode())
        .isEqualTo(RosettaErrorType.STAKING_KEY_MISSING.getCode());
  }

  @Test
  void getOperationsFromTransactionData()
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

    List<Operation> operations = operationService
        .getOperationsFromTransactionData(transactionData, NetworkEnum.MAINNET.getNetwork());

    assertThat(operations).hasSize(2);
    assertThat(operations.get(1).getType()).isEqualTo(OperationType.WITHDRAWAL.getValue());
    assertThat(operations.get(1).getOperationIdentifier().getIndex()).isEqualTo(22L);
    assertThat(operations.get(1).getAmount().getValue()).isEqualTo("value");
    assertThat(operations.get(1).getMetadata().getStakingCredential().getHexBytes())
        .isEqualTo("1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F");
  }

  private static Operation creteTestPoolRegistrationOperation(String accountAddress, String rewardAddress,
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
    Operation operation1 = creteTestPoolRegistrationOperation("addr1", "rewardAddress", List.of("poolOwner1", "poolOwner2"));
    Operation operation2 = creteTestPoolRegistrationOperation("addr2", "rewardAddress", List.of("poolOwner3", "poolOwner4"));
    TransactionExtraData transactionExtraData = new TransactionExtraData(List.of(operation1, operation2), "81a100a0");

    return new TransactionData(
        TransactionBody.builder().build(),
            transactionExtraData
    );
  }

}
