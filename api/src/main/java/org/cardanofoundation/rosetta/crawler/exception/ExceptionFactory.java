package org.cardanofoundation.rosetta.crawler.exception;


import static org.cardanofoundation.rosetta.crawler.util.RosettaConstants.RosettaErrorType;


public class ExceptionFactory {

  public static ApiException blockNotFoundException() {
    return new ApiException(RosettaErrorType.BLOCK_NOT_FOUND.toRosettaError(false));
  }
  public static ApiException unspecifiedError(String details) {
    return new ApiException(RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
        Details.builder().message(details).build()));
  }

  public static ApiException invalidAddressError(String address) {
    return new ApiException(RosettaErrorType.INVALID_ADDRESS.toRosettaError(true, address));
  }
}
