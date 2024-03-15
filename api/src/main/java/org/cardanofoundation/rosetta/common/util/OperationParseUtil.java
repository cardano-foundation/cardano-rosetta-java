package org.cardanofoundation.rosetta.common.util;

import static java.math.BigInteger.valueOf;

import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.Withdrawal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Set;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRegistrationCertReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRetirement;
import org.cardanofoundation.rosetta.common.model.cardano.pool.ProcessPoolRegistrationReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.ProcessWithdrawalReturn;
import org.cardanofoundation.rosetta.common.model.cardano.pool.StakeCertificate;
import org.jetbrains.annotations.NotNull;
import org.openapitools.client.model.Operation;

public class OperationParseUtil {

  public static ProcessOperations parseOperation(
      Operation operation, NetworkIdentifierType networkIdentifierType, ProcessOperations resultAccumulator, String type) throws CborSerializationException, CborDeserializationException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException {
    if (type.equals(OperationType.INPUT.getValue())) {
      return parseTypeInput(operation, resultAccumulator);
    }
    if (type.equals(OperationType.OUTPUT.getValue())) {
      return parseTypeOutput(operation, resultAccumulator);
    }
    if (type.equals(OperationType.STAKE_KEY_REGISTRATION.getValue())) {
      return parseStakeKeyRegistration(operation, resultAccumulator);
    }
    if (type.equals(OperationType.STAKE_KEY_DEREGISTRATION.getValue())) {
      return parseTypeStakeKeyDeregistration(operation, networkIdentifierType, resultAccumulator);
    }
    if (type.equals(OperationType.STAKE_DELEGATION.getValue())) {
      return parseStakeDelegation(operation, networkIdentifierType, resultAccumulator);
    }
    if (type.equals(OperationType.WITHDRAWAL.getValue())) {
      return parseWithdrawal(operation, networkIdentifierType, resultAccumulator);
    }
    if (type.equals(OperationType.POOL_REGISTRATION.getValue())) {
      return parsePoolRegistration(operation, resultAccumulator);
    }
    if (type.equals(OperationType.POOL_REGISTRATION_WITH_CERT.getValue())) {
      return parsePoolRegistrationWithCert(operation, networkIdentifierType, resultAccumulator);
    }
    if (type.equals(OperationType.POOL_RETIREMENT.getValue())) {
      return parsePoolRetirement(operation, resultAccumulator);
    }
    if (type.equals(OperationType.VOTE_REGISTRATION.getValue())) {
      return parseVoteRegistration(operation, resultAccumulator);
    }
    return null;
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
    resultAccumulator.getCertificates().add(ProcessContructionUtil.getStakeRegistrationCertificateFromOperation(
        operation));
    double stakeNumber = resultAccumulator.getStakeKeyRegistrationsCount();
    resultAccumulator.setStakeKeyRegistrationsCount(++stakeNumber);
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseTypeStakeKeyDeregistration(Operation operation,
      NetworkIdentifierType networkIdentifierType, ProcessOperations resultAccumulator) {
    StakeCertificate stakeCertificateDto = ProcessContructionUtil.getStakeCertificateFromOperation(
        networkIdentifierType, operation);
    resultAccumulator.getCertificates().add(stakeCertificateDto.getCertificate());
    resultAccumulator.getAddresses().add(stakeCertificateDto.getAddress());
    double stakeNumber = resultAccumulator.getStakeKeyDeRegistrationsCount();
    resultAccumulator.setStakeKeyDeRegistrationsCount(++stakeNumber);
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseStakeDelegation(Operation operation,
      NetworkIdentifierType networkIdentifierType, ProcessOperations resultAccumulator) {
    StakeCertificate stakeCertificateDto = ProcessContructionUtil.getStakeCertificateFromOperation(
        networkIdentifierType, operation);
    resultAccumulator.getCertificates().add(stakeCertificateDto.getCertificate());
    resultAccumulator.getAddresses().add(stakeCertificateDto.getAddress());
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseWithdrawal(Operation operation,
      NetworkIdentifierType networkIdentifierType, ProcessOperations resultAccumulator) {
    ProcessWithdrawalReturn processWithdrawalReturnDto = ProcessContructionUtil.getWithdrawalsReturnFromOperation(
        networkIdentifierType, operation);
    String withdrawalAmountString = ValidateParseUtil.validateValueAmount(operation);
    assert withdrawalAmountString != null;
    long withdrawalAmount = Long.parseLong(withdrawalAmountString);
    resultAccumulator.getWithdrawalAmounts().add(withdrawalAmount);
    resultAccumulator.getWithdrawals().add(new Withdrawal(processWithdrawalReturnDto.getReward().getAddress(),
        valueOf(withdrawalAmount)));
    resultAccumulator.getAddresses().add(processWithdrawalReturnDto.getAddress());
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parseVoteRegistration(Operation operation,
      ProcessOperations resultAccumulator) {
    AuxiliaryData voteRegistrationMetadata = ProcessContructionUtil.processVoteRegistration(
        operation);
    resultAccumulator.setVoteRegistrationMetadata(voteRegistrationMetadata);
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parsePoolRetirement(Operation operation,
      ProcessOperations resultAccumulator) {
    PoolRetirement poolRetirementDto = ProcessContructionUtil.getPoolRetirementFromOperation(
        operation);
    resultAccumulator.getCertificates().add(poolRetirementDto.getCertificate());
    resultAccumulator.getAddresses().add(poolRetirementDto.getPoolKeyHash());
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parsePoolRegistrationWithCert(Operation operation,
      NetworkIdentifierType networkIdentifierType, ProcessOperations resultAccumulator) {
    PoolRegistrationCertReturn dto = ProcessContructionUtil.getPoolRegistrationCertFromOperation(
        operation,
        networkIdentifierType);
    resultAccumulator.getCertificates().add(dto.getCertificate());
    Set<String> set = dto.getAddress();
    resultAccumulator.getAddresses().addAll(set);
    double poolNumber = resultAccumulator.getPoolRegistrationsCount();
    resultAccumulator.setPoolRegistrationsCount(++poolNumber);
    return resultAccumulator;
  }

  @NotNull
  private static ProcessOperations parsePoolRegistration(Operation operation,
      ProcessOperations resultAccumulator) {
    ProcessPoolRegistrationReturn processPoolRegistrationReturnDto = ProcessContructionUtil.getPoolRegistrationFromOperation(
        operation);
    resultAccumulator.getCertificates().add(processPoolRegistrationReturnDto.getCertificate());
    resultAccumulator.getAddresses()
        .addAll(processPoolRegistrationReturnDto.getTotalAddresses());
    double poolNumber = resultAccumulator.getPoolRegistrationsCount();
    resultAccumulator.setPoolRegistrationsCount(++poolNumber);
    return resultAccumulator;
  }

}
