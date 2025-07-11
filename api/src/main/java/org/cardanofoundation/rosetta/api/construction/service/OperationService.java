package org.cardanofoundation.rosetta.api.construction.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import org.apache.commons.lang3.ObjectUtils;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;

import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRegistrationCertReturn;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.ParseConstructionUtil;
import org.cardanofoundation.rosetta.common.util.ValidateParseUtil;

import static org.cardanofoundation.rosetta.common.util.Constants.OPERATION_TYPE_POOL_REGISTRATION;
import static org.cardanofoundation.rosetta.common.util.Constants.OPERATION_TYPE_POOL_REGISTRATION_WITH_CERT;

/**
 * Service class to operate on Operations
 */
@Slf4j
@Service
public class OperationService {

  public List<Operation> getOperationsFromTransactionData(TransactionData data, Network network)
      throws CborDeserializationException, CborException, CborSerializationException {
    TransactionBody transactionBody = data.transactionBody();
    TransactionExtraData extraData = data.transactionExtraData();

    List<Operation> operations = new ArrayList<>();
    fillInputOperations(transactionBody, extraData, operations);
    fillOutputOperations(transactionBody, operations);
    fillCertOperations(transactionBody, extraData, network, operations);
    fillWithdrawalOperations(transactionBody, extraData, network, operations);
    fillVoteOperations(extraData, operations);

    return operations;
  }

  public List<String> getSignerFromOperation(Network network, Operation operation) {
    if (Constants.POOL_OPERATIONS.contains(operation.getType())) {
      return getPoolSigners(network, operation);
    }
    if (operation.getAccount() != null) {
      // org.openapitools.client.model.AccountIdentifier.getAddress() is always not null
      return Collections.singletonList(operation.getAccount().getAddress());
    }
    if (operation.getType().equals(OperationType.VOTE_REGISTRATION.getValue())) {
      return Collections.emptyList();
    }
    validateMetadataForStakingCredential(operation);
    HdPublicKey hdPublicKey =
        CardanoAddressUtils.publicKeyToHdPublicKey(operation.getMetadata().getStakingCredential());

    return Collections.singletonList(
        CardanoAddressUtils.generateRewardAddress(network, hdPublicKey));
  }

  private void fillInputOperations(TransactionBody transactionBody,
      TransactionExtraData extraData,
      List<Operation> operations) {
    List<TransactionInput> inputs = transactionBody.getInputs();
    log.info("[fillInputOperations] About to parse {} inputs", inputs.size());
    List<Operation> extraDataInputOperations = extraData.operations().stream()
        .filter(o -> o.getType().equals(OperationType.INPUT.getValue()))
        .toList();

    for (int i = 0; i < inputs.size(); i++) {
      if (!extraDataInputOperations.isEmpty() && extraDataInputOperations.size() <= inputs.size()) {
        operations.add(extraDataInputOperations.get(i));
      } else { // fallback in case of no extra data input operations
        TransactionInput input = inputs.get(i);

        Operation inputParsed = ParseConstructionUtil.transactionInputToOperation(input,
            (long) operations.size());

        operations.add(inputParsed);
      }
    }
  }

  private void fillOutputOperations(TransactionBody transactionBody, List<Operation> operations) {
    List<TransactionOutput> outputs = transactionBody.getOutputs();
    List<OperationIdentifier> relatedOperations = ParseConstructionUtil.getRelatedOperationsFromInputs(
        operations);

    log.info("[parseOperationsFromTransactionBody] About to parse {} outputs", outputs.size());

    for (TransactionOutput output : outputs) {
      Operation outputParsed = ParseConstructionUtil.transActionOutputToOperation(output,
          (long) operations.size(),
          relatedOperations);
      operations.add(outputParsed);
    }
  }

  private void fillCertOperations(TransactionBody transactionBody, TransactionExtraData extraData,
      Network network, List<Operation> operations)
      throws CborException, CborSerializationException {
    List<Operation> certOps = extraData.operations().stream()
        .filter(o -> {
                    return Constants.STAKE_POOL_OPERATIONS.contains(o.getType())
                            || OperationType.VOTE_DREP_DELEGATION.getValue().equals(o.getType());
                }
        ).toList();

    List<Operation> parsedCertOperations = ParseConstructionUtil.parseCertsToOperations(
            transactionBody, certOps, network);

    operations.addAll(parsedCertOperations);
  }

  private void fillWithdrawalOperations(TransactionBody transactionBody,
      TransactionExtraData extraData,
      Network network, List<Operation> operations) {
    List<Operation> withdrawalOps = extraData.operations().stream()
        .filter(o -> o.getType().equals(OperationType.WITHDRAWAL.getValue()))
        .toList();
    int withdrawalsCount = ObjectUtils.isEmpty(transactionBody.getWithdrawals()) ? 0
        : transactionBody.getWithdrawals().size();
    List<Operation> withdrawalsOperations = ParseConstructionUtil.parseWithdrawalsToOperations(
            withdrawalOps, withdrawalsCount, network);
    operations.addAll(withdrawalsOperations);
  }

  // TODO catalyst voting renaming?
  private void fillVoteOperations(TransactionExtraData extraData, List<Operation> operations)
      throws CborDeserializationException {
    List<Operation> voteOp = extraData.operations().stream()
        .filter(o -> o.getType().equals(OperationType.VOTE_REGISTRATION.getValue()))
        .toList();
    if (!ObjectUtils.isEmpty(voteOp)) {
      Operation parsedVoteOperations = ParseConstructionUtil.parseVoteMetadataToOperation(
          voteOp.getFirst().getOperationIdentifier().getIndex(),
          extraData.transactionMetadataHex()
      );
      operations.add(parsedVoteOperations);
    }
  }

  private List<String> getPoolSigners(Network network, Operation operation) {
    List<String> signers = new ArrayList<>();
    switch (operation.getType()) {
      case OPERATION_TYPE_POOL_REGISTRATION -> {
        if (ValidateParseUtil.validateAddressPresence(operation)) {
          signers.add(operation.getAccount().getAddress());
        }
        Optional.ofNullable(operation.getMetadata())
            .map(OperationMetadata::getPoolRegistrationParams)
            .ifPresent(poolRegistrationParameters -> {
              signers.add(poolRegistrationParameters.getRewardAddress());
              signers.addAll(poolRegistrationParameters.getPoolOwners());
            });
      }
      case OPERATION_TYPE_POOL_REGISTRATION_WITH_CERT -> {
        String poolCertAsHex = Optional.ofNullable(operation.getMetadata())
            .map(OperationMetadata::getPoolRegistrationCert)
            .orElse(null);
        PoolRegistrationCertReturn dto = ValidateParseUtil.validateAndParsePoolRegistrationCert(
            network,
            poolCertAsHex,
            operation.getAccount() == null ? null : operation.getAccount().getAddress()
        );
        signers.addAll(dto.getAddress());
      }

      // pool retirement case
      default -> {
        if (ValidateParseUtil.validateAddressPresence(operation)) {
          signers.add(operation.getAccount().getAddress());
        }
      }
    }
    log.info("[getPoolSigners] About to return {} signers for {} operation", signers.size(),
        operation.getType());

    return signers;
  }

  private void validateMetadataForStakingCredential(Operation operation) {
    if (operation.getMetadata() == null) {
      throw ExceptionFactory.missingStakingKeyError();
    }
  }
}
