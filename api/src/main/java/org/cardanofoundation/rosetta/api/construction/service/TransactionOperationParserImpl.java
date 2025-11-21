package org.cardanofoundation.rosetta.api.construction.service;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.cert.Certificate;
import com.bloxbean.cardano.client.util.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.model.cardano.pool.PoolRegistrationCertReturn;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.common.util.ParseConstructionUtil;
import org.cardanofoundation.rosetta.common.util.ValidateParseUtil;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.cardanofoundation.rosetta.common.enumeration.OperationType.POOL_GOVERNANCE_VOTE;
import static org.cardanofoundation.rosetta.common.enumeration.OperationType.VOTE_DREP_DELEGATION;
import static org.cardanofoundation.rosetta.common.util.Constants.*;
import static org.cardanofoundation.rosetta.common.util.OperationTypes.*;
import static org.cardanofoundation.rosetta.common.util.ParseConstructionUtil.*;

/**
 * Implementation of {@link TransactionOperationParser} for parsing Rosetta operations.
 * <p>
 * This parser orchestrates the conversion of Cardano transaction structures into
 * Rosetta API operations. It handles multiple operation types and ensures proper
 * ordering and relationships between operations.
 * </p>
 */
@Slf4j
@Service
public class TransactionOperationParserImpl implements TransactionOperationParser {

  @Override
  public List<Operation> getOperationsFromTransactionData(TransactionData data, Network network) throws CborException, CborSerializationException {
    TransactionBody transactionBody = data.transactionBody();
    TransactionExtraData extraData = data.transactionExtraData();

    List<Operation> operations = new ArrayList<>();

    fillInputOperations(transactionBody, extraData, operations);
    fillOutputOperations(transactionBody, operations);
    fillCertOperations(transactionBody, extraData, network, operations);
    fillWithdrawalOperations(transactionBody, extraData, network, operations);
    // if more voting operations are added consider to rename this to fillVotingOperations
    fillSpoVotingOperations(extraData, operations);

    operations.sort(Comparator.comparingLong(op -> op.getOperationIdentifier().getIndex()));

    return operations;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Signer determination logic:
   * <ul>
   *   <li><b>Pool operations:</b> Delegates to {@link #getPoolSigners(Network, Operation)}
   *       which extracts pool owners and reward addresses</li>
   *   <li><b>Operations with account:</b> Returns the account address directly</li>
   *   <li><b>Staking operations:</b> Derives reward address from staking credential</li>
   * </ul>
   * </p>
   */
  @Override
  public List<String> getSignerFromOperation(Network network, Operation operation) {
    // Pool operations require special handling for multiple signers
    if (POOL_OPERATIONS.contains(operation.getType())) {
      return getPoolSigners(network, operation);
    }

    // If account is present, use its address directly
    if (operation.getAccount() != null) {
      // org.openapitools.client.model.AccountIdentifier.getAddress() is always not null
      return Collections.singletonList(operation.getAccount().getAddress());
    }

    // For staking operations, derive reward address from credential
    validateMetadataForStakingCredential(operation);

    HdPublicKey hdPublicKey =
            CardanoAddressUtils.publicKeyToHdPublicKey(operation.getMetadata().getStakingCredential());

    return Collections.singletonList(
            CardanoAddressUtils.generateRewardAddress(network, hdPublicKey));
  }

  /**
   * Processes transaction inputs and converts them to Rosetta input operations.
   * <p>
   * Input operations represent UTxO consumption. This method attempts to use
   * pre-processed input operations from extra data if available, otherwise
   * falls back to parsing directly from transaction inputs.
   * </p>
   *
   * @param transactionBody the transaction body containing inputs
   * @param extraData extra transaction data that may contain pre-processed input operations
   * @param operations the list to append input operations to
   */
  private void fillInputOperations(TransactionBody transactionBody,
                                   TransactionExtraData extraData,
                                   List<Operation> operations) {
    List<TransactionInput> inputs = transactionBody.getInputs();
    log.info("[fillInputOperations] About to parse {} inputs", inputs.size());

    // Try to use pre-processed input operations from extra data
    List<Operation> extraDataInputOperations = extraData.operations().stream()
            .filter(o -> o.getType().equals(OperationType.INPUT.getValue()))
            .toList();

    for (int i = 0; i < inputs.size(); i++) {
      if (i < extraDataInputOperations.size()) {
        // Use pre-processed operation from extra data
        operations.add(extraDataInputOperations.get(i));
      } else {
        // Fallback: parse directly from transaction input
        TransactionInput input = inputs.get(i);
        Operation inputParsed = ParseConstructionUtil.transactionInputToOperation(input,
                (long) operations.size());

        operations.add(inputParsed);
      }
    }
  }

  /**
   * Processes transaction outputs and converts them to Rosetta output operations.
   * <p>
   * Output operations represent UTxO creation. Each output includes related operations
   * linking it to the corresponding input operations for proper transaction flow visualization.
   * </p>
   *
   * @param transactionBody the transaction body containing outputs
   * @param operations the list to append output operations to
   */
  private void fillOutputOperations(TransactionBody transactionBody,
                                    List<Operation> operations) {
    List<TransactionOutput> outputs = transactionBody.getOutputs();

    if (outputs == null) {
      log.warn("[fillOutputOperations] Transaction outputs list is null, skipping output operations");
      return;
    }

    List<OperationIdentifier> relatedOperations = ParseConstructionUtil.getRelatedOperationsFromInputs(
            operations);

    log.info("[fillOutputOperations] About to parse {} outputs", outputs.size());

    for (TransactionOutput output : outputs) {
      Operation outputParsed = ParseConstructionUtil.transActionOutputToOperation(output,
              (long) operations.size(),
              relatedOperations);

      operations.add(outputParsed);
    }
  }

  /**
   * Processes transaction certificates and converts them to Rosetta certificate operations.
   * <p>
   * This method handles ALL certificate types in a single pass, maintaining the order
   * of certificates as they appear in the transaction body. Certificate types include:
   * <ul>
   *   <li><b>Staking certificates:</b> Registration, deregistration, delegation</li>
   *   <li><b>Pool certificates:</b> Pool registration, retirement</li>
   *   <li><b>Governance certificates:</b> DRep vote delegation (VoteDelegCert)</li>
   * </ul>
   * </p>
   * <p>
   * <b>Important:</b> The order of operations in extraData must match the order of
   * certificates in the transaction body's certs[] array.
   * </p>
   *
   * @param transactionBody the transaction body containing certificates
   * @param extraData extra transaction data containing certificate operation metadata
   * @param network the Cardano network for address generation
   * @param operations the list to append certificate operations to
   * @throws CborException if CBOR processing fails
   * @throws CborSerializationException if certificate serialization fails
   */
  private void fillCertOperations(TransactionBody transactionBody,
                                  TransactionExtraData extraData,
                                  Network network,
                                  List<Operation> operations)
      throws CborException, CborSerializationException {

    // Filter ALL certificate operations from extra data
    List<Operation> certOps = extraData.operations().stream()
        .filter(o -> CERTIFICATE_OPERATIONS.contains(o.getType()))
        .toList();

    List<Certificate> certs = transactionBody.getCerts();
    int certsCount = getCertSize(certs);

    log.info("[fillCertOperations] About to parse {} certificates", certsCount);

    // Process all certificates in order
    for (int i = 0; i < certsCount; i++) {
      Operation certOperation = certOps.get(i);
      Certificate cert = ValidateParseUtil.validateCert(certs, i);

      if (ObjectUtils.isEmpty(cert)) {
        continue;
      }


      // Handle staking operations (registration, deregistration, delegation)
      if (STAKING_OPERATIONS.contains(certOperation.getType())) {
        String hex = getStakingCredentialHex(certOperation);
        HdPublicKey hdPublicKey = new HdPublicKey();
        hdPublicKey.setKeyData(HexUtil.decodeHexString(hex));
        String address = CardanoAddressUtils.generateRewardAddress(network, hdPublicKey);

        Operation parsedOperation = parseStakingCertToOperation(
                cert,
                certOperation.getOperationIdentifier().getIndex(),
                hex,
                certOperation.getType(),
                address
        );

        parsedOperation.setAccount(certOperation.getAccount());
        operations.add(parsedOperation);
      }

      // Handle DRep vote delegation (governance certificate)
      if (VOTE_DREP_DELEGATION.getValue().equals(certOperation.getType())) {
        String hex = getStakingCredentialHex(certOperation);
        HdPublicKey hdPublicKey = new HdPublicKey();
        hdPublicKey.setKeyData(HexUtil.decodeHexString(hex));
        String address = CardanoAddressUtils.generateRewardAddress(network, hdPublicKey);

        Operation parsedOperation = parseDRepVoteDelegation(
                cert,
                certOperation.getOperationIdentifier().getIndex(),
                hex,
                certOperation.getType(),
                address
        );

        parsedOperation.setAccount(certOperation.getAccount());
        operations.add(parsedOperation);
      }

      // Handle pool operations (registration, retirement)
      if (POOL_OPERATIONS.contains(certOperation.getType())) {
        Operation parsedOperation = parsePoolCertToOperation(
                network,
                cert,
                certOperation.getOperationIdentifier().getIndex(),
                certOperation.getType()
        );
        parsedOperation.setAccount(certOperation.getAccount());
        operations.add(parsedOperation);
      }

    }
  }

  /**
   * Processes staking reward withdrawals and converts them to Rosetta withdrawal operations.
   * <p>
   * Withdrawal operations represent the claiming of staking rewards accumulated in
   * reward accounts. The method validates that the number of operations matches the
   * number of withdrawals in the transaction body.
   * </p>
   *
   * @param transactionBody the transaction body containing withdrawals
   * @param extraData extra transaction data containing withdrawal operation metadata
   * @param network the Cardano network for address validation
   * @param operations the list to append withdrawal operations to
   */
  private void fillWithdrawalOperations(TransactionBody transactionBody,
                                        TransactionExtraData extraData,
                                        Network network,
                                        List<Operation> operations) {
    List<Operation> withdrawalOps = extraData.operations().stream()
            .filter(o -> o.getType().equals(OperationType.WITHDRAWAL.getValue()))
            .toList();

    int withdrawalsCount = ObjectUtils.isEmpty(transactionBody.getWithdrawals()) ? 0
            : transactionBody.getWithdrawals().size();

    log.info("[parseWithdrawalsToOperations] About to parse {} withdrawals", withdrawalsCount);

    for (int i = 0; i < withdrawalsCount; i++) {
      Operation withdrawalOperation = withdrawalOps.get(i);
      String stakingCredentialHex = getStakingCredentialHex(withdrawalOperation);
      HdPublicKey hdPublicKey = new HdPublicKey();
      hdPublicKey.setKeyData(HexUtil.decodeHexString(stakingCredentialHex));
      String address = CardanoAddressUtils.generateRewardAddress(network, hdPublicKey);
      Operation parsedOperation = parseWithdrawalToOperation(
              withdrawalOperation.getAmount().getValue(),
              stakingCredentialHex,
              withdrawalOperation.getOperationIdentifier().getIndex(),
              address
      );
      operations.add(parsedOperation);
    }
  }

  /**
   * Processes SPO (Stake Pool Operator) governance voting operations from transaction extra data.
   * <p>
   * This method handles pool governance votes which are voting procedures (NOT certificates).
   * These operations represent stake pool operators voting on governance actions through
   * the Conway era voting mechanism.
   * </p>
   * <p>
   * <b>Important distinction:</b>
   * <ul>
   *   <li><b>Pool Governance Votes (this method):</b> Voting procedures in the voting_procedures
   *       field of the transaction body. Operation type: {@code poolGovernanceVote}</li>
   *   <li><b>DRep Vote Delegation:</b> Certificate-based operations (VoteDelegCert) processed
   *       in {@link #fillCertOperations(TransactionBody, TransactionExtraData, Network, List)}.
   *       Operation type: {@code dRepVoteDelegation}</li>
   * </ul>
   * </p>
   * <p>
   * Unlike certificates, SPO voting operations come pre-processed in extra data with all
   * necessary metadata already populated, including:
   * <ul>
   *   <li><b>governance_action_hash</b> - The hash of the governance action being voted on</li>
   *   <li><b>pool_credential</b> - The stake pool's credential (public key)</li>
   *   <li><b>vote</b> - The vote choice (yes, no, abstain)</li>
   * </ul>
   * Therefore, this method simply filters and adds these fully-formed operations to the list.
   * </p>
   *
   * @param extraData extra transaction metadata containing pre-processed SPO voting operations
   * @param operations the list to append SPO voting operations to
   */
  private void fillSpoVotingOperations(TransactionExtraData extraData,
                                       List<Operation> operations) {
    // Filter only SPO governance voting operations from extra data
    // These are voting procedures, not certificates
    List<Operation> spoVotingOps = extraData.operations().stream()
            .filter(o -> o.getType().equals(POOL_GOVERNANCE_VOTE.getValue()))
            .toList();

    log.info("[fillSpoVotingOperations] About to add {} SPO voting operations", spoVotingOps.size());

    // SPO voting operations are already fully formed in extra data, just add them
    operations.addAll(spoVotingOps);
  }

  /**
   * Extracts all required signers for pool-related operations.
   * <p>
   * Pool operations may require multiple signers depending on the operation type:
   * <ul>
   *   <li><b>Pool registration:</b> Pool owners, reward address, optional payment address</li>
   *   <li><b>Pool registration with cert:</b> Extracted from the certificate hex</li>
   *   <li><b>Pool retirement:</b> Payment address if present</li>
   * </ul>
   * </p>
   *
   * @param network the Cardano network for address validation
   * @param operation the pool operation to extract signers from
   * @return list of addresses that must sign the pool operation
   */
  private List<String> getPoolSigners(Network network, Operation operation) {
    List<String> signers = new ArrayList<>();

    switch (operation.getType()) {
      case OPERATION_TYPE_POOL_REGISTRATION -> {
        // Add payment address if present
        if (ValidateParseUtil.validateAddressPresence(operation)) {
          signers.add(operation.getAccount().getAddress());
        }
        // Extract pool owners and reward address from registration params
        Optional.ofNullable(operation.getMetadata())
                .map(OperationMetadata::getPoolRegistrationParams)
                .ifPresent(poolRegistrationParameters -> {
                  signers.add(poolRegistrationParameters.getRewardAddress());
                  signers.addAll(poolRegistrationParameters.getPoolOwners());
                });
      }
      case OPERATION_TYPE_POOL_REGISTRATION_WITH_CERT -> {
        // Parse signers from the pool registration certificate hex
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
      case OPERATION_TYPE_POOL_RETIREMENT -> {
        // Add payment address if present for pool retirement
        if (ValidateParseUtil.validateAddressPresence(operation)) {
          signers.add(operation.getAccount().getAddress());
        }
      }

      default -> throw new IllegalStateException("pool operation not supported, operation:" + operation.getType());
    }

    log.info("[getPoolSigners] About to return {} signers for {} operation", signers.size(), operation.getType());

    return signers;
  }

  /**
   * Validates that the operation metadata contains a staking credential.
   * <p>
   * This validation is required for operations that need to derive a reward address
   * from the staking credential (e.g., stake registration, delegation).
   * </p>
   *
   * @param operation the operation to validate
   * @throws org.cardanofoundation.rosetta.common.exception.ApiException if metadata is null
   */
  private void validateMetadataForStakingCredential(Operation operation) {
    if (operation.getMetadata() == null) {
      throw ExceptionFactory.missingStakingKeyError();
    }
  }
}
