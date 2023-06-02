package org.cardanofoundation.rosetta.api.exception;


import static org.cardanofoundation.rosetta.api.util.RosettaConstants.RosettaErrorType;


public class ExceptionFactory {

  public static ApiException blockNotFoundException() {
    return new ApiException(RosettaErrorType.BLOCK_NOT_FOUND.toRosettaError(false));
  }

  public static ApiException genesisBlockNotFound() {
    return new ApiException(RosettaErrorType.GENESIS_BLOCK_NOT_FOUND.toRosettaError(false));
  }
  public static ServerException configNotFoundException(){
    return new ServerException("Environment configurations needed to run server were not found");
  }

  public static ApiException invalidBlockChainError() {
    return new ApiException(RosettaErrorType.INVALID_BLOCKCHAIN.toRosettaError(false));
  }

  public static ApiException networkNotFoundError() {
    return new ApiException(RosettaErrorType.NETWORK_NOT_FOUND.toRosettaError(false));
  }

  public static ApiException unspecifiedError(String details) {
    return new ApiException(RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
        Details.builder().message(details).build()));
  }
  public static ApiException invalidAddressError(String address) {
    return new ApiException(RosettaErrorType.INVALID_ADDRESS.toRosettaError(true, address));
  }
  public static ApiException missingStakingKeyError() {
    return new ApiException(RosettaErrorType.STAKING_KEY_MISSING.toRosettaError(false));
  }
  public static ApiException invalidBlockchainError() {
    return new ApiException(RosettaErrorType.INVALID_BLOCKCHAIN.toRosettaError(false));
  }
  public static ApiException invalidAddressError() {
    return new ApiException(RosettaErrorType.INVALID_ADDRESS.toRosettaError(false));
  }
  public static ApiException cantCreateSignTransaction() {
    return new ApiException(RosettaErrorType.CANT_CREATE_SIGN_TRANSACTION.toRosettaError(false));
  }
  public static ApiException cantBuildSignedTransaction() {
    return new ApiException(RosettaErrorType.CANT_CREATE_SIGN_TRANSACTION.toRosettaError(false));
  }
  public static ApiException missingChainCodeError() {
    return new ApiException(RosettaErrorType.CHAIN_CODE_MISSING.toRosettaError(false));
  }
  public static ApiException cantBuildWitnessesSet() {
    return new ApiException(RosettaErrorType.CANT_BUILD_WITNESSES_SET.toRosettaError(false));
  }
  public static ApiException outputsAreBiggerThanInputsError() {
    return new ApiException(RosettaErrorType.OUTPUTS_BIGGER_THAN_INPUTS_ERROR.toRosettaError(false));
  }
  public static ApiException invalidOperationTypeError() {
    return new ApiException(RosettaErrorType.INVALID_OPERATION_TYPE.toRosettaError(false));
  }
  public static ApiException missingPoolKeyError() {
    return new ApiException(RosettaErrorType.POOL_KEY_MISSING.toRosettaError(false));
  }
  public static ApiException missingPoolCertError() {
    return new ApiException(RosettaErrorType.POOL_CERT_MISSING.toRosettaError(false));
  }
  public static ApiException invalidPoolRegistrationCert(Object error) {
    return new ApiException(RosettaErrorType.INVALID_POOL_CERT.toRosettaError(false,error));
  }
  public static ApiException invalidPoolRegistrationCertType() {
    return new ApiException(RosettaErrorType.INVALID_POOL_CERT_TYPE.toRosettaError(false));
  }
  public static ApiException invalidPublicKeyFormat() {
    return new ApiException(RosettaErrorType.INVALID_PUBLIC_KEY_FORMAT.toRosettaError(false));
  }
  public static ApiException missingVoteRegistrationMetadata() {
    return new ApiException(RosettaErrorType.MISSING_VOTE_REGISTRATION_METADATA.toRosettaError(false));
  }
  public static ApiException invalidStakingKeyFormat() {
    return new ApiException(RosettaErrorType.INVALID_STAKING_KEY_FORMAT.toRosettaError(false));
  }
  public static ApiException votingNonceNotValid() {
    return new ApiException(RosettaErrorType.VOTING_NONCE_NOT_VALID.toRosettaError(false));
  }
  public static ApiException invalidVotingSignature() {
    return new ApiException(RosettaErrorType.INVALID_VOTING_SIGNATURE.toRosettaError(false));
  }
  public static ApiException missingVotingKeyError() {
    return new ApiException(RosettaErrorType.MISSING_VOTING_KEY.toRosettaError(false));
  }
  public static ApiException invalidVotingKeyFormat() {
    return new ApiException(RosettaErrorType.INVALID_VOTING_KEY_FORMAT.toRosettaError(false));
  }
  public static ApiException missingMetadataParametersForPoolRetirement() {
    return new ApiException(RosettaErrorType.MISSING_METADATA_PARAMETERS_FOR_POOL_RETIREMENT.toRosettaError(false));
  }
  public static ApiException invalidPoolKeyError() {
    return new ApiException(RosettaErrorType.INVALID_POOL_KEY_HASH.toRosettaError(false));
  }
  public static ApiException missingPoolRegistrationParameters() {
    return new ApiException(RosettaErrorType.POOL_REGISTRATION_PARAMS_MISSING.toRosettaError(false));
  }
  public static ApiException invalidPoolMetadataError() {
    return new ApiException(RosettaErrorType.INVALID_POOL_METADATA.toRosettaError(false));
  }
  public static ApiException invalidPoolRelaysError() {
    return new ApiException(RosettaErrorType.INVALID_POOL_RELAYS.toRosettaError(false));
  }
  public static ApiException invalidPoolRelayTypeError() {
    return new ApiException(RosettaErrorType.INVALID_POOL_RELAY_TYPE.toRosettaError(false));
  }
  public static ApiException invalidIpv4() {
    return new ApiException(RosettaErrorType.INVALID_IPV4_ERROR.toRosettaError(false));
  }
  public static ApiException missingDnsNameError() {
    return new ApiException(RosettaErrorType.DNS_NAME_MISSING.toRosettaError(false));
  }
  public static ApiException invalidPoolRelaysError(Object error) {
    return new ApiException(RosettaErrorType.INVALID_POOL_RELAYS.toRosettaError(false,error));
  }
  public static ApiException invalidPoolRegistrationParameters(Object error) {
    return new ApiException(RosettaErrorType.INVALID_POOL_RELAYS.toRosettaError(false,error));
  }
  public static ApiException addressGenerationError() {
    return new ApiException(RosettaErrorType.ADDRESS_GENERATION_ERROR.toRosettaError(false));
  }
  public static ApiException transactionInputsParametersMissingError(Object error) {
    return new ApiException(RosettaErrorType.TRANSACTION_INPUTS_PARAMETERS_MISSING_ERROR.toRosettaError(false,error));
  }
  public static ApiException transactionOutputDeserializationError(Object error) {
    return new ApiException(RosettaErrorType.TRANSACTION_OUTPUT_DESERIALIZATION_ERROR.toRosettaError(false,error));
  }
  public static ApiException transactionOutputsParametersMissingError(Object error) {
    return new ApiException(RosettaErrorType.TRANSACTION_OUTPUTS_PARAMETERS_MISSING_ERROR.toRosettaError(false,error));
  }
  public static ApiException cantCreateUnsignedTransactionFromBytes() {
    return new ApiException(RosettaErrorType.CANT_CREATE_UNSIGNED_TRANSACTION_ERROR.toRosettaError(false));
  }
  public static ApiException cantCreateSignedTransactionFromBytes() {
    return new ApiException(RosettaErrorType.CANT_CREATE_SIGNED_TRANSACTION_ERROR.toRosettaError(false));
  }
  public static ApiException tokenBundleAssetsMissingError() {
    return new ApiException(RosettaErrorType.TOKEN_BUNDLE_ASSETS_MISSING.toRosettaError(false));
  }
  public static ApiException tokenAssetValueMissingError() {
    return new ApiException(RosettaErrorType.TOKEN_ASSET_VALUE_MISSING.toRosettaError(false));
  }
  public static ApiException parseSignedTransactionError() {
    return new ApiException(RosettaErrorType.PARSE_SIGNED_TRANSACTION_ERROR.toRosettaError(false));
  }
  public static ApiException invalidPoolOwnersError(Object error) {
    return new ApiException(RosettaErrorType.INVALID_POOL_OWNERS.toRosettaError(false,error));
  }
  public static ApiException invalidAddressTypeError() {
    return new ApiException(RosettaErrorType.INVALID_ADDRESS_TYPE.toRosettaError(false));
  }

  public static ApiException transactionNotFound() {
    return new ApiException(RosettaErrorType.TRANSACTION_NOT_FOUND.toRosettaError(false));
  }
  public static ApiException invalidTokenNameError(String details) {
    return new ApiException(RosettaErrorType.INVALID_TOKEN_NAME.toRosettaError(false, details));
  }
  public static ApiException invalidPolicyIdError(String details) {
    return new ApiException(RosettaErrorType.INVALID_POLICY_ID.toRosettaError(false, details));
  }
}
