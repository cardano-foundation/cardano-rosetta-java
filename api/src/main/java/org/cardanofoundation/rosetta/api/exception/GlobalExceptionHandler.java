package org.cardanofoundation.rosetta.api.exception;

import org.cardanofoundation.rosetta.api.util.RosettaConstants.RosettaErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

  @Value("#{systemProperties['api.exception.isPrintStackTrace'] ?: 'false'}")
  private String isPrintStackTrace;


  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> handleGlobalException(Exception exception,
      WebRequest webRequest) {

    if (isPrintStackTrace.equals("true")) {
      exception.printStackTrace();
    }

    Error errorResponse;

    if (ApiException.class.isInstance(exception)) {
      ApiException apiException = (ApiException) exception;
      errorResponse = apiException.getError();

    } else {
      errorResponse = RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
          Details.builder().message(
              "An error occurred for request " + webRequest.getSessionId() + ": "
                  + exception.getMessage()).build());
    }

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
