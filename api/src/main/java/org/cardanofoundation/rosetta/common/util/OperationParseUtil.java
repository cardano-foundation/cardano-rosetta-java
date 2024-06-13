package org.cardanofoundation.rosetta.common.util;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Set;
import jakarta.validation.constraints.NotNull;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.Withdrawal;
import org.apache.commons.lang3.ObjectUtils;
import org.openapitools.client.model.Operation;

import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRegistrationCertReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRetirement;
import org.cardanofoundation.rosetta.common.model.cardano.pool.ProcessPoolRegistrationReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.ProcessWithdrawalReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.StakeCertificate;

public class OperationParseUtil {

  private OperationParseUtil() {
  }

  public static ProcessOperations parseOperation(
      Operation operation, Network network, ProcessOperations resultAccumulator, String type) {
    return switch (OperationType.fromValue(type)) {
      case OperationType.INPUT ->
          parseTypeInput(operation, resultAccumulator);
      case OperationType.OUTPUT ->
          parseTypeOutput(operation, resultAccumulator);
      case OperationType.STAKE_KEY_REGISTRATION ->
          parseStakeKeyRegistration(operation, resultAccumulator);
      case OperationType.STAKE_KEY_DEREGISTRATION ->
          parseTypeStakeKeyDeregistration(operation, network, resultAccumulator);
      case OperationType.STAKE_DELEGATION ->
          parseStakeDelegation(operation, network, resultAccumulator);
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
      case null -> null;
      // Without the default case, the switch statement would not compile in case of adding a new enum value
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
    resultAccumulator.getCertificates().add(ProcessConstructionUtil.getStakeRegistrationCertificateFromOperation(
        operation));
    double stakeNumber = resultAccumulator.getStakeKeyRegistrationsCount();
    resultAccumulator.setStakeKeyRegistrationsCount(++stakeNumber);
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseTypeStakeKeyDeregistration(Operation operation,
      Network network, ProcessOperations resultAccumulator) {
    StakeCertificate stakeCertificate = ProcessConstructionUtil.getStakeCertificateFromOperation(network, operation);
    resultAccumulator.getCertificates().add(stakeCertificate.getCertificate());
    resultAccumulator.getAddresses().add(stakeCertificate.getAddress());
    double stakeNumber = resultAccumulator.getStakeKeyDeRegistrationsCount();
    resultAccumulator.setStakeKeyDeRegistrationsCount(++stakeNumber);
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseStakeDelegation(Operation operation,
      Network network, ProcessOperations resultAccumulator) {
    StakeCertificate stakeCertificate = ProcessConstructionUtil.getStakeCertificateFromOperation(network, operation);
    resultAccumulator.getCertificates().add(stakeCertificate.getCertificate());
    resultAccumulator.getAddresses().add(stakeCertificate.getAddress());
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseWithdrawal(Operation operation, Network network, ProcessOperations resultAccumulator) {
    ProcessWithdrawalReturn processWithdrawalReturn = ProcessConstructionUtil.getWithdrawalsReturnFromOperation(
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
    AuxiliaryData voteRegistrationMetadata = ProcessConstructionUtil.processVoteRegistration(
        operation);
    resultAccumulator.setVoteRegistrationMetadata(voteRegistrationMetadata);
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parsePoolRetirement(Operation operation,
      ProcessOperations resultAccumulator) {
    PoolRetirement poolRetirement = ProcessConstructionUtil.getPoolRetirementFromOperation(
        operation);
    resultAccumulator.getCertificates().add(poolRetirement.getCertificate());
    resultAccumulator.getAddresses().add(poolRetirement.getPoolKeyHash());
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parsePoolRegistrationWithCert(Operation operation,
      Network network, ProcessOperations resultAccumulator) {
    PoolRegistrationCertReturn poolRegistrationCertReturn = ProcessConstructionUtil.getPoolRegistrationCertFromOperation(
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
    ProcessPoolRegistrationReturn processPoolRegistrationReturn = ProcessConstructionUtil.getPoolRegistrationFromOperation(
        operation);
    resultAccumulator.getCertificates().add(processPoolRegistrationReturn.getCertificate());
    resultAccumulator.getAddresses()
        .addAll(processPoolRegistrationReturn.getTotalAddresses());
    double poolNumber = resultAccumulator.getPoolRegistrationsCount();
    resultAccumulator.setPoolRegistrationsCount(++poolNumber);
    return resultAccumulator;
  }

}
