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
    return new ApiException(RosettaErrorType.NETWORKS_NOT_FOUND.toRosettaError(false));
  }

  public static ApiException unspecifiedError(String details) {
    return new ApiException(RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
        Details.builder().message(details).build()));
  }
  public static ApiException invalidAddressError(String address) {
    return new ApiException(RosettaErrorType.INVALID_ADDRESS.toRosettaError(true, address));
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
