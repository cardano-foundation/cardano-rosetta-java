package org.cardanofoundation.rosetta.common.mapper;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.util.HexUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRegistrationCertReturn;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.ParseConstructionUtil;
import org.cardanofoundation.rosetta.common.util.ValidateParseUtil;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.PoolRegistrationParams;

/**
 * This whole class will be rewritten with mapper. For the initial implementation it's fine.
 */
@Slf4j
public class TransactionDataToOperations {
  private TransactionDataToOperations() {

  }

  public static List<Operation> convert(TransactionData data,
      Integer network)
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

  private static void fillVoteOperations(TransactionExtraData extraData, List<Operation> operations)
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

  private static void fillWithdrawalOperations(TransactionBody transactionBody, TransactionExtraData extraData,
      Integer network, List<Operation> operations) {
    List<Operation> withdrawalOps = extraData.operations().stream()
        .filter(o -> o.getType().equals(OperationType.WITHDRAWAL.getValue()))
        .toList();
    int withdrawalsCount = ObjectUtils.isEmpty(transactionBody.getWithdrawals()) ? 0
        : transactionBody.getWithdrawals().size();
    List<Operation> withdrawalsOperations = ParseConstructionUtil.parseWithdrawalsToOperations(withdrawalOps,
        withdrawalsCount, network);
    operations.addAll(withdrawalsOperations);
  }

  private static void fillCertOperations(TransactionBody transactionBody, TransactionExtraData extraData,
      Integer network, List<Operation> operations)
      throws CborException, CborSerializationException {
    List<Operation> certOps = extraData.operations().stream()
        .filter(o -> Constants.STAKE_POOL_OPERATIONS.contains(o.getType())
        ).toList();
    List<Operation> parsedCertOperations = ParseConstructionUtil.parseCertsToOperations(
        transactionBody, certOps,
        network);
    operations.addAll(parsedCertOperations);
  }

  private static void fillOutputOperations(TransactionBody transactionBody, List<Operation> operations) {
    List<TransactionOutput> outputs = transactionBody.getOutputs();
    List<OperationIdentifier> relatedOperations = ParseConstructionUtil.getRelatedOperationsFromInputs(
        operations);
    log.info("[parseOperationsFromTransactionBody] About to parse {} outputs", outputs.size());
    for (TransactionOutput output : outputs) {
      Operation outputParsed = ParseConstructionUtil.TransActionOutputToOperation(output, (long) operations.size(),
          relatedOperations);
      operations.add(outputParsed);
    }
  }

  private static void fillInputOperations(TransactionBody transactionBody, TransactionExtraData extraData,
      List<Operation> operations) {
    List<TransactionInput> inputs = transactionBody.getInputs();
    log.info("[parseOperationsFromTransactionBody] About to parse {} inputs", inputs.size());
    List<Operation> inputOperations = extraData.operations().stream()
        .filter(o -> o.getType().equals(OperationType.INPUT.getValue()))
        .toList();
    for (int i = 0; i < inputs.size(); i++) {
      if (!inputOperations.isEmpty() && inputOperations.size() <= inputs.size()) {
        operations.add(inputOperations.get(i));
      } else {
        TransactionInput input = inputs.get(i);
        Operation inputParsed = ParseConstructionUtil.TransactionInputToOperation(input, (long) operations.size());
        operations.add(inputParsed);
      }
    }
  }
  public static List<String> getSignerFromOperation(NetworkIdentifierType networkIdentifierType,
      Operation operation) {
    if (Constants.POOL_OPERATIONS.contains(operation.getType())) {
      return getPoolSigners(networkIdentifierType, operation);
    }
    if (!ObjectUtils.isEmpty(
        ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress())) {
      return new ArrayList<>(List.of(operation.getAccount().getAddress()));
    }
    if (operation.getType().equals(OperationType.VOTE_REGISTRATION.getValue())) {
      return new ArrayList<>();
    }
    HdPublicKey hdPublicKey = new HdPublicKey();
    hdPublicKey.setKeyData(
        HexUtil.decodeHexString(operation.getMetadata().getStakingCredential().getHexBytes()));
    return new ArrayList<>(List.of(
        CardanoAddressUtils.generateRewardAddress(networkIdentifierType, hdPublicKey)));
  }
  public static List<String> getPoolSigners(NetworkIdentifierType networkIdentifierType,
      Operation operation) {
    List<String> signers = new ArrayList<>();
    switch (operation.getType()) {
      case "poolRegistration" -> {
        PoolRegistrationParams poolRegistrationParameters =
            ObjectUtils.isEmpty(operation.getMetadata()) ? null
                : operation.getMetadata().getPoolRegistrationParams();
        if (ValidateParseUtil.validateAddressPresence(operation)) {
          signers.add(operation.getAccount().getAddress());
        }
        if (poolRegistrationParameters != null) {
          signers.add(poolRegistrationParameters.getRewardAddress());
          signers.addAll(poolRegistrationParameters.getPoolOwners());
        }
      }
      case "poolRegistrationWithCert" -> {
        String poolCertAsHex = Optional.ofNullable(operation.getMetadata())
            .map(OperationMetadata::getPoolRegistrationCert)
            .orElse(null);
        PoolRegistrationCertReturn dto = ValidateParseUtil.validateAndParsePoolRegistrationCert(
            networkIdentifierType,
            poolCertAsHex,
            ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress()
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

  public static List<AccountIdentifier> getUniqueAccountIdentifiers(List<String> addresses) {
    return addressesToAccountIdentifiers(new HashSet<>(addresses));
  }

  public static List<AccountIdentifier> addressesToAccountIdentifiers(Set<String> uniqueAddresses) {
    return uniqueAddresses.stream().map(s -> new AccountIdentifier(s, null, null)).toList();
  }
}
