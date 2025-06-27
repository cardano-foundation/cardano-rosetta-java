package org.cardanofoundation.rosetta.common.util;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import lombok.extern.slf4j.Slf4j;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.Withdrawal;
import com.bloxbean.cardano.client.util.HexUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.api.block.model.domain.GovernanceVote;
import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRegistrationCertReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRetirement;
import org.cardanofoundation.rosetta.common.model.cardano.pool.ProcessPoolRegistrationReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.ProcessWithdrawalReturn;

@Slf4j
public class OperationParseUtil {

  @Nullable
  // TODO change so it returns optional
  public static ProcessOperations parseOperation(Operation operation,
                                                 Network network,
                                                 ProcessOperations resultAccumulator,
                                                 String type) {
    if (type == null) {
      return null;
    }

    log.debug("OperationParseUtil.parseOperation, type:" + type);

    OperationType operationType = OperationType.fromValue(type);
    if (operationType == null) {
      return null;
    }

    return switch (OperationType.fromValue(type)) {
      case OperationType.INPUT ->
              parseTypeInput(operation, resultAccumulator);
      case OperationType.OUTPUT ->
              parseTypeOutput(operation, resultAccumulator);
      case OperationType.STAKE_KEY_REGISTRATION ->
              parseStakeKeyRegistration(operation, resultAccumulator);
      case OperationType.STAKE_KEY_DEREGISTRATION ->
              parseTypeStakeKeyDeregistration(operation, network, resultAccumulator);
      case OperationType.STAKE_DELEGATION, VOTE_DREP_DELEGATION ->
              parseCertificate(operation, network, resultAccumulator);
      case OperationType.WITHDRAWAL ->
              parseWithdrawal(operation, network, resultAccumulator);
      case OperationType.POOL_REGISTRATION ->
              parsePoolRegistration(operation, resultAccumulator);
      case OperationType.POOL_REGISTRATION_WITH_CERT ->
              parsePoolRegistrationWithCert(operation, network, resultAccumulator);
      case OperationType.POOL_RETIREMENT ->
              parsePoolRetirement(operation, resultAccumulator);
      case OperationType.VOTE_REGISTRATION ->
              parseVoteRegistration(operation, resultAccumulator);
      case OperationType.POOL_GOVERNANCE_VOTE ->
              parsePoolGovernanceVote(operation, resultAccumulator);
    };
  }

  @NotNull
  private static ProcessOperations parseTypeInput(Operation operation,
                                                  ProcessOperations resultAccumulator) {
    resultAccumulator.getTransactionInputs().add(ValidateParseUtil.validateAndParseTransactionInput(
            operation));
    resultAccumulator.getAddresses().add(ObjectUtils.isEmpty(
            operation.getAccount()) ? null : operation.getAccount().getAddress());
    resultAccumulator.getInputAmounts().add(ValidateParseUtil.validateValueAmount(operation));

    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseTypeOutput(Operation operation,
                                                   ProcessOperations resultAccumulator) {
    resultAccumulator.getTransactionOutputs().add(ValidateParseUtil.validateAndParseTransactionOutput(
            operation));
    resultAccumulator.getOutputAmounts().add(ValidateParseUtil.validateValueAmount(operation));

    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseStakeKeyRegistration(Operation operation,
                                                             ProcessOperations resultAccumulator) {
    resultAccumulator.getCertificates().add(ProcessConstructions.getStakeRegistrationCertificateFromOperation(
            operation));
    double stakeNumber = resultAccumulator.getStakeKeyRegistrationsCount();
    resultAccumulator.setStakeKeyRegistrationsCount(++stakeNumber);

    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseTypeStakeKeyDeregistration(Operation operation,
                                                                   Network network,
                                                                   ProcessOperations resultAccumulator) {
    ProcessConstructions.getCertificateFromOperation(network, operation).ifPresent(certificateWithAddress -> {
      resultAccumulator.getCertificates().add(certificateWithAddress.getCertificate());
      resultAccumulator.getAddresses().add(certificateWithAddress.getAddress());

      double stakeNumber = resultAccumulator.getStakeKeyDeRegistrationsCount();

      resultAccumulator.setStakeKeyDeRegistrationsCount(++stakeNumber);
    });

    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseCertificate(
          Operation operation,
          Network network,
          ProcessOperations resultAccumulator) {

    ProcessConstructions.getCertificateFromOperation(network, operation).ifPresent(certificateWithAddress -> {
      resultAccumulator.getCertificates().add(certificateWithAddress.getCertificate());
      resultAccumulator.getAddresses().add(certificateWithAddress.getAddress());
    });

    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseWithdrawal(Operation operation, Network network, ProcessOperations resultAccumulator) {
    ProcessWithdrawalReturn processWithdrawalReturn = ProcessConstructions.getWithdrawalsReturnFromOperation(
            network, operation);

    BigInteger withdrawalAmount = ValidateParseUtil.validateValueAmount(operation);
    Objects.requireNonNull(withdrawalAmount, "No withdrawal amount found in operation");

    resultAccumulator.getWithdrawalAmounts().add(withdrawalAmount);
    resultAccumulator.getWithdrawals().add(new Withdrawal(processWithdrawalReturn.getReward().getAddress(),
            withdrawalAmount));

    resultAccumulator.getAddresses().add(processWithdrawalReturn.getAddress());

    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseVoteRegistration(Operation operation,
                                                         ProcessOperations resultAccumulator) {
    AuxiliaryData voteRegistrationMetadata = ProcessConstructions.processVoteRegistration(
            operation);
    resultAccumulator.setVoteRegistrationMetadata(voteRegistrationMetadata);

    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parsePoolRetirement(Operation operation,
                                                       ProcessOperations resultAccumulator) {
    PoolRetirement poolRetirement = ProcessConstructions.getPoolRetirementFromOperation(
            operation);
    resultAccumulator.getCertificates().add(poolRetirement.getCertificate());
    resultAccumulator.getAddresses().add(poolRetirement.getPoolKeyHash());

    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parsePoolRegistrationWithCert(Operation operation,
                                                                 Network network,
                                                                 ProcessOperations resultAccumulator) {
    PoolRegistrationCertReturn poolRegistrationCertReturn = ProcessConstructions.getPoolRegistrationCertFromOperation(
            operation, network);

    resultAccumulator.getCertificates().add(poolRegistrationCertReturn.getCertificate());

    Set<String> set = poolRegistrationCertReturn.getAddress();

    resultAccumulator.getAddresses().addAll(set);

    double poolNumber = resultAccumulator.getPoolRegistrationsCount();

    resultAccumulator.setPoolRegistrationsCount(++poolNumber);

    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parsePoolRegistration(Operation operation,
                                                         ProcessOperations resultAccumulator) {
    ProcessPoolRegistrationReturn processPoolRegistrationReturn = ProcessConstructions.getPoolRegistrationFromOperation(
            operation);
    resultAccumulator.getCertificates().add(processPoolRegistrationReturn.getCertificate());

    resultAccumulator.getAddresses()
            .addAll(processPoolRegistrationReturn.getTotalAddresses());

    double poolNumber = resultAccumulator.getPoolRegistrationsCount();

    resultAccumulator.setPoolRegistrationsCount(++poolNumber);

    return resultAccumulator;
  }

  private static ProcessOperations parsePoolGovernanceVote(Operation operation,
                                                           ProcessOperations processOperations) {
    AccountIdentifier account = operation.getAccount();
    String poolKeyHash = account.getAddress();

    if (CardanoAddressUtils.isValidBech32(poolKeyHash)) {
      poolKeyHash = HexUtil.encodeHexString(Bech32.decode(poolKeyHash).data);
    }

    processOperations.getAddresses().add(poolKeyHash);

    // at this point we should no longer have the bech32 of hashed address, only pool hash
    ValidateParseUtil.validateAndParsePoolKeyHash(poolKeyHash);

    Optional.of(operation.getMetadata().getPoolGovernanceVoteParams()).ifPresent(poolGovernanceVoteParams -> {
      GovernanceVote governanceVote = ProcessConstructions.processGovernanceVote(operation);

      processOperations.getGovernanceVotes().add(governanceVote);
    });

    return processOperations;
  }

}
