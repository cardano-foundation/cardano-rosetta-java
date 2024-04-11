package org.cardanofoundation.rosetta.common.util;

import java.util.List;

import org.cardanofoundation.rosetta.common.exception.Details;
import org.cardanofoundation.rosetta.common.exception.Error;
import org.openapitools.client.model.OperationStatus;

public class RosettaConstants {

  public static final String BLOCKCHAIN_NAME = "cardano";

  public static final OperationStatus INVALID_OPERATION_STATUS = buildOperationStatus("invalid",
      false);
  public static final OperationStatus SUCCESS_OPERATION_STATUS = buildOperationStatus("success",
      true);

  public static final List<OperationStatus> ROSETTA_OPERATION_STATUSES = List.of(
      SUCCESS_OPERATION_STATUS, INVALID_OPERATION_STATUS
  );

  public static final List<String> ROSETTA_OPERATION_TYPES = List.of(
      "input",
      "output",
      "stakeKeyRegistration",
      "stakeDelegation",
      "withdrawal",
      "stakeKeyDeregistration",
      "poolRegistration",
      "poolRegistrationWithCert",
      "poolRetirement",
      "voteRegistration"
  );
  public static final List<Error> ROSETTA_ERRORS = List.of(
      RosettaErrorType.BLOCK_NOT_FOUND.toRosettaError(false),
      RosettaErrorType.INVALID_BLOCKCHAIN.toRosettaError(false),
      RosettaErrorType.NETWORK_NOT_FOUND.toRosettaError(false),
      RosettaErrorType.NETWORKS_NOT_FOUND.toRosettaError(false),
      RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true),
      RosettaErrorType.NOT_IMPLEMENTED.toRosettaError(false),
      RosettaErrorType.GENESIS_BLOCK_NOT_FOUND.toRosettaError(false),
      RosettaErrorType.TRANSACTION_NOT_FOUND.toRosettaError(false),
      RosettaErrorType.ADDRESS_GENERATION_ERROR.toRosettaError(false),
      RosettaErrorType.INVALID_PUBLIC_KEY_FORMAT.toRosettaError(false),
      RosettaErrorType.INVALID_STAKING_KEY_FORMAT.toRosettaError(false),
      RosettaErrorType.STAKING_KEY_MISSING.toRosettaError(false),
      RosettaErrorType.CHAIN_CODE_MISSING.toRosettaError(false),
      RosettaErrorType.DNS_NAME_MISSING.toRosettaError(false),
      RosettaErrorType.POOL_CERT_MISSING.toRosettaError(false),
      RosettaErrorType.POOL_KEY_MISSING.toRosettaError(false),
      RosettaErrorType.POOL_REGISTRATION_PARAMS_MISSING.toRosettaError(false),
      RosettaErrorType.INVALID_POOL_CERT.toRosettaError(false),
      RosettaErrorType.INVALID_POOL_CERT_TYPE.toRosettaError(false),
      RosettaErrorType.INVALID_POOL_KEY_HASH.toRosettaError(false),
      RosettaErrorType.INVALID_POOL_RELAYS.toRosettaError(false),
      RosettaErrorType.INVALID_POOL_REGISTRATION_PARAMS.toRosettaError(false),
      RosettaErrorType.INVALID_POOL_RELAY_TYPE.toRosettaError(false),
      RosettaErrorType.INVALID_POOL_OWNERS.toRosettaError(false),
      RosettaErrorType.INVALID_POOL_METADATA.toRosettaError(false),
      RosettaErrorType.PARSE_SIGNED_TRANSACTION_ERROR.toRosettaError(false),
      RosettaErrorType.CANT_BUILD_WITNESSES_SET.toRosettaError(false),
      RosettaErrorType.CANT_CREATE_SIGN_TRANSACTION.toRosettaError(false),
      RosettaErrorType.TRANSACTION_INPUTS_PARAMETERS_MISSING_ERROR.toRosettaError(false),
      RosettaErrorType.TRANSACTION_OUTPUTS_PARAMETERS_MISSING_ERROR.toRosettaError(false),
      RosettaErrorType.OUTPUTS_BIGGER_THAN_INPUTS_ERROR.toRosettaError(false),
      RosettaErrorType.MISSING_METADATA_PARAMETERS_FOR_POOL_RETIREMENT.toRosettaError(false),
      RosettaErrorType.CANT_CREATE_SIGNED_TRANSACTION_ERROR.toRosettaError(false),
      RosettaErrorType.CANT_CREATE_UNSIGNED_TRANSACTION_ERROR.toRosettaError(false),
      RosettaErrorType.SEND_TRANSACTION_ERROR.toRosettaError(true),
      RosettaErrorType.TRANSACTION_INPUT_DESERIALIZATION_ERROR.toRosettaError(false),
      RosettaErrorType.TRANSACTION_OUTPUT_DESERIALIZATION_ERROR.toRosettaError(false),
      RosettaErrorType.INVALID_ADDRESS.toRosettaError(true),
      RosettaErrorType.INVALID_ADDRESS_TYPE.toRosettaError(true),
      RosettaErrorType.INVALID_OPERATION_TYPE.toRosettaError(true),
      RosettaErrorType.INVALID_POLICY_ID.toRosettaError(false),
      RosettaErrorType.INVALID_TOKEN_NAME.toRosettaError(false),
      RosettaErrorType.TOKEN_BUNDLE_ASSETS_MISSING.toRosettaError(false),
      RosettaErrorType.TOKEN_ASSET_VALUE_MISSING.toRosettaError(false),
      RosettaErrorType.OUTSIDE_VALIDITY_INTERVAL_UTXO.toRosettaError(false),
      RosettaErrorType.VOTING_NONCE_NOT_VALID.toRosettaError(false),
      RosettaErrorType.INVALID_VOTING_SIGNATURE.toRosettaError(false),
      RosettaErrorType.MISSING_VOTING_KEY.toRosettaError(false),
      RosettaErrorType.INVALID_VOTING_KEY_FORMAT.toRosettaError(false),
      RosettaErrorType.MISSING_VOTE_REGISTRATION_METADATA.toRosettaError(false),
      RosettaErrorType.INVALID_OPERATION_STATUS.toRosettaError(false),
      RosettaErrorType.STATUS_SUCCESS_MATCH_ERROR.toRosettaError(false),
      RosettaErrorType.TX_HASH_COIN_NOT_MATCH.toRosettaError(false),
      RosettaErrorType.ADDRESS_AND_ACCOUNT_ID_NOT_MATCH.toRosettaError(false),
      RosettaErrorType.BAD_FORMED_COIN_ERROR.toRosettaError(false)
  );

  private static OperationStatus buildOperationStatus(final String status,
      final boolean successful) {
    final OperationStatus operationStatus = new OperationStatus();
    operationStatus.setStatus(status);
    operationStatus.setSuccessful(successful);
    return operationStatus;
  }

  public enum RosettaErrorType {
    INVALID_NETWORK("Invalid Network configuration", 4000),
    BLOCK_NOT_FOUND("Block not found", 4001),
    NETWORK_NOT_FOUND("Network not found", 4002),
    NETWORKS_NOT_FOUND("Networks not found", 4003),
    INVALID_BLOCKCHAIN("Invalid blockchain", 4004),
    GENESIS_BLOCK_NOT_FOUND("Genesis block not found", 4005),

    INVALID_PUBLIC_KEY_FORMAT("Invalid public key format", 4007),
    TRANSACTION_NOT_FOUND("Transaction not found", 4006),
    PUBLIC_KEY_MISSING("Public key is missing", 4008),
    TRANSACTION_INPUTS_PARAMETERS_MISSING_ERROR(
        "Transaction inputs parameters errors in operations array", 4008),
    TRANSACTION_OUTPUTS_PARAMETERS_MISSING_ERROR(
        "Transaction outputs parameters errors in operations array", 4009),
    OUTPUTS_BIGGER_THAN_INPUTS_ERROR(
        "The transaction you are trying to build has more outputs than inputs", 4010),
    CANT_CREATE_SIGNED_TRANSACTION_ERROR("Cant create signed transaction from transaction bytes",
        4011),
    CANT_CREATE_UNSIGNED_TRANSACTION_ERROR(
        "Cant create unsigned transaction from transaction bytes", 4012),
    CANT_ENCODE_EXTRA_DATA("Cant encode extra data", 4012),
    TRANSACTION_INPUT_DESERIALIZATION_ERROR(
        "Cant deserialize transaction input from transaction body", 4013),
    TRANSACTION_OUTPUT_DESERIALIZATION_ERROR(
        "Cant deserialize transaction output from transaction body", 4014),
    INVALID_ADDRESS("Provided address is invalid", 4015),
    INVALID_ADDRESS_TYPE("Provided address type is invalid", 4016),
    INVALID_STAKING_KEY_FORMAT("Invalid staking key format", 4017),
    STAKING_KEY_MISSING("Staking key is required for this type of address", 4018),
    INVALID_OPERATION_TYPE("Provided operation type is invalid", 4019),
    POOL_KEY_MISSING("Pool key hash is required to operate", 4020),
    TOKEN_BUNDLE_ASSETS_MISSING("Assets are required for output operation token bundle", 4021),
    TOKEN_ASSET_VALUE_MISSING("Asset value is required for token asset", 4022),
    INVALID_POLICY_ID("Invalid policy id", 4023),
    INVALID_TOKEN_NAME("Invalid token name", 4024),
    INVALID_POOL_KEY_HASH("Provided pool key hash has invalid format", 4025),
    POOL_CERT_MISSING("Pool registration certificate is required for pool registration", 4026),
    INVALID_POOL_CERT("Invalid pool registration certificate format", 4027),
    INVALID_POOL_CERT_TYPE("Invalid certificate type. Expected pool registration certificate",
        4028),
    POOL_REGISTRATION_PARAMS_MISSING("Pool registration parameters were expected", 4029),
    INVALID_POOL_RELAYS("Pool relays are invalid", 4030),
    INVALID_POOL_METADATA("Pool metadata is invalid", 4031),
    DNS_NAME_MISSING("Dns name expected for pool relay", 4032),
    INVALID_POOL_RELAY_TYPE("Invalid pool relay type received", 4033),
    INVALID_POOL_OWNERS("Invalid pool owners received", 4034),
    INVALID_POOL_REGISTRATION_PARAMS("Invalid pool registration parameters received", 4035),
    MISSING_METADATA_PARAMETERS_FOR_POOL_RETIREMENT("Mandatory parameter is missing: Epoch", 4036),
    OUTSIDE_VALIDITY_INTERVAL_UTXO(
        "Error when sending the transaction - OutsideValidityIntervalUTxO", 4037),
    CONFIG_NOT_FOUND("Environment configurations needed to run server were not found", 4038),
    UNSPECIFIED_ERROR("An error occurred", 5000),
    NOT_IMPLEMENTED("Not implemented", 5001),
    ADDRESS_GENERATION_ERROR("Address generation error", 5002),
    PARSE_SIGNED_TRANSACTION_ERROR("Parse signed transaction error", 5003),
    CANT_CREATE_SIGN_TRANSACTION(
        "Cant create signed transaction probably because of unsigned transaction bytes", 5004),
    CANT_BUILD_WITNESSES_SET(
        "Cant build witnesses set for transaction probably because of provided signatures", 5005),
    SEND_TRANSACTION_ERROR("Error when sending the transaction", 5006),
    VOTING_NONCE_NOT_VALID("Voting nonce not valid", 5007),
    INVALID_VOTING_SIGNATURE("Invalid voting signature", 5008),
    MISSING_VOTING_KEY("Voting key is missing", 5009),
    INVALID_VOTING_KEY_FORMAT("Voting key format is invalid", 5010),
    MISSING_VOTE_REGISTRATION_METADATA("Missing vote registration metadata", 5011),
    CHAIN_CODE_MISSING("Missing chain code", 5012),
    INVALID_OPERATION_STATUS("Invalid operation status", 5013),
    STATUS_SUCCESS_MATCH_ERROR("Given operation status and success state does not match", 5014),
    TX_HASH_COIN_NOT_MATCH("Transaction hash does not match to given coin identifier", 5015),
    ADDRESS_AND_ACCOUNT_ID_NOT_MATCH("Address and account identifier does not match", 5016),
    BAD_FORMED_COIN_ERROR("Coin identifier has an invalid format", 5017),
    CANT_DECODE_MEMPOOL_TRANSACTION("Cant decode mempool transaction", 5018),
    INVALID_TRANSACTION("Can't decode Transaction", 5019);

    final String message;
    final int code;

    RosettaErrorType(final String message, final int code) {
      this.code = code;
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    public int getCode() {
      return code;
    }

    public Error toRosettaError(final boolean retriable) {
      return toRosettaError(retriable, null, null);
    }

    public Error toRosettaError(final boolean retriable, final Details details) {
      return toRosettaError(retriable, details, null);
    }

    public Error toRosettaError(final boolean retriable, final Details details,
        final String description) {
      final Error error = new Error();
      error.setCode(code);
      error.setMessage(message);
      error.setRetriable(retriable);
      error.setDescription(description);
      error.setDetails(details);
      return error;
    }
  }
}
