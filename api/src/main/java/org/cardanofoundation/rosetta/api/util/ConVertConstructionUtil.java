package org.cardanofoundation.rosetta.api.util;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.util.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.OperationIdentifier;
import org.cardanofoundation.rosetta.api.model.PoolRegistrationParams;
import org.cardanofoundation.rosetta.api.model.TransactionExtraData;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.projection.dto.PoolRegistrationCertReturnDto;

@Slf4j
public class ConVertConstructionUtil {

  public static List<Operation> convert(TransactionBody transactionBody,
      TransactionExtraData extraData,
      Integer network)
      throws UnknownHostException, JsonProcessingException, AddressExcepion, CborDeserializationException, CborException, CborSerializationException {
    List<Operation> operations = new ArrayList<>();
    List<TransactionInput> inputs = transactionBody.getInputs();
    List<TransactionOutput> outputs = transactionBody.getOutputs();
    log.info("[parseOperationsFromTransactionBody] About to parse {} inputs", inputs.size());
    List<Operation> inputOperations = extraData.getOperations().stream()
        .filter(o -> o.getType().equals(OperationType.INPUT.getValue()))
        .toList();
    for (int i = 0; i < inputs.size(); i++) {

      if (!inputOperations.isEmpty() && inputOperations.size() <= inputs.size()) {
        Operation operation = new Operation();
        operation.setOperationIdentifier(inputOperations.get(i).getOperationIdentifier());
        operation.setRelatedOperations(inputOperations.get(i).getRelatedOperations());
        operation.setType(inputOperations.get(i).getType());
        operation.setStatus("");
        operation.setAccount(inputOperations.get(i).getAccount());
        operation.setAmount(inputOperations.get(i).getAmount());
        operation.setCoinChange(inputOperations.get(i).getCoinChange());
        operation.setMetadata(inputOperations.get(i).getMetadata());
        operations.add(operation);
      } else {
        TransactionInput input = inputs.get(i);
        Operation inputParsed = ParseConstructionUtils.parseInputToOperation(input, (long) operations.size());
        Operation operation = new Operation();
        operation.setOperationIdentifier(inputParsed.getOperationIdentifier());
        operation.setRelatedOperations(inputParsed.getRelatedOperations());
        operation.setType(inputParsed.getType());
        operation.setStatus("");
        operation.setAccount(inputParsed.getAccount());
        operation.setAmount(inputParsed.getAmount());
        operation.setCoinChange(inputParsed.getCoinChange());
        operation.setMetadata(inputParsed.getMetadata());
        operations.add(operation);
      }
    }
    // till this line operations only contains inputs
    List<OperationIdentifier> relatedOperations = getRelatedOperationsFromInputs(operations);
    log.info("[parseOperationsFromTransactionBody] About to parse {} outputs", outputs.size());
    for (TransactionOutput output : outputs) {
      String address = ParseConstructionUtils.parseAddress(output.getAddress(), CardanoAddressUtils.getAddressPrefix(network, null));
      Operation outputParsed = ParseConstructionUtils.parseOutputToOperation(output, (long) operations.size(),
          relatedOperations, address);
      operations.add(outputParsed);
    }

    List<Operation> certOps = extraData.getOperations().stream()
        .filter(o -> Constants.StakePoolOperations.contains(o.getType())
        ).toList();
    List<Operation> parsedCertOperations = ParseConstructionUtils.parseCertsToOperations(transactionBody, certOps,
        network);
    operations.addAll(parsedCertOperations);
    List<Operation> withdrawalOps = extraData.getOperations().stream()
        .filter(o -> o.getType().equals(OperationType.WITHDRAWAL.getValue()))
        .toList();
    Integer withdrawalsCount = ObjectUtils.isEmpty(transactionBody.getWithdrawals()) ? 0
        : transactionBody.getWithdrawals().size();
    ParseConstructionUtils.parseWithdrawalsToOperations(withdrawalOps, withdrawalsCount, operations, network);

    List<Operation> voteOp = extraData.getOperations().stream()
        .filter(o -> o.getType().equals(OperationType.VOTE_REGISTRATION.getValue()))
        .toList();
    if (!ObjectUtils.isEmpty(voteOp)) {
      Operation parsedVoteOperations = ParseConstructionUtils.parseVoteMetadataToOperation(
          voteOp.get(0).getOperationIdentifier().getIndex(),
          extraData.getTransactionMetadataHex()
      );
      operations.add(parsedVoteOperations);
    }

    return operations;
  }
  public static List<OperationIdentifier> getRelatedOperationsFromInputs(List<Operation> inputs) {
    return inputs.stream()
        .map(input -> new OperationIdentifier(input.getOperationIdentifier().getIndex(), null))
        .toList();
  }
  public static List<String> getSignerFromOperation(NetworkIdentifierType networkIdentifierType,
      Operation operation) throws CborSerializationException {
    if (Constants.PoolOperations.contains(operation.getType())) {
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
    return new ArrayList<>(List.of(CardanoAddressUtils.generateRewardAddress(networkIdentifierType, hdPublicKey)));
  }
  public static List<String> getPoolSigners(NetworkIdentifierType networkIdentifierType,
      Operation operation) {
    List<String> signers = new ArrayList<>();
    switch (operation.getType()) {
      case "poolRegistration" -> {
        PoolRegistrationParams poolRegistrationParameters =
            ObjectUtils.isEmpty(operation.getMetadata()) ? null
                : operation.getMetadata().getPoolRegistrationParams();
        if (ValidateOfConstruction.validateAddressPresence(operation)) {
          signers.add(operation.getAccount().getAddress());
        }
        if (poolRegistrationParameters != null) {
          signers.add(poolRegistrationParameters.getRewardAddress());
          signers.addAll(poolRegistrationParameters.getPoolOwners());
        }
      }
      case "poolRegistrationWithCert" -> {
        String poolCertAsHex = ObjectUtils.isEmpty(operation.getMetadata()) ? null
            : operation.getMetadata().getPoolRegistrationCert();
        PoolRegistrationCertReturnDto dto = ValidateOfConstruction.validateAndParsePoolRegistrationCert(
            networkIdentifierType,
            poolCertAsHex,
            ObjectUtils.isEmpty(operation.getAccount()) ? null : operation.getAccount().getAddress()
        );
        signers.addAll(dto.getAddress());
      }

      // pool retirement case
      default -> {
        if (ValidateOfConstruction.validateAddressPresence(operation)) {
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
    return uniqueAddresses.stream().map(AccountIdentifier::new).toList();
  }
}
