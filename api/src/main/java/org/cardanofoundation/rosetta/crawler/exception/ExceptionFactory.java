package org.cardanofoundation.rosetta.crawler.exception;


import static org.cardanofoundation.rosetta.crawler.util.RosettaConstants.RosettaErrorType;


public class ExceptionFactory {

  public static ApiException blockNotFoundException() {
    return new ApiException(RosettaErrorType.BLOCK_NOT_FOUND.toRosettaError(false));
  }
}
