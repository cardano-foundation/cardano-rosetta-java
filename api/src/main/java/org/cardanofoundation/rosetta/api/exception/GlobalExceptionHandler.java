package org.cardanofoundation.rosetta.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.cardanofoundation.rosetta.api.util.RosettaConstants.RosettaErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @Value("${api.exception.isPrintStackTrace:false}")
  private String isPrintStackTrace;


  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> handleGlobalException(Exception exception,
      HttpServletRequest request) {

    if (isPrintStackTrace.equals("true")) {
      exception.printStackTrace();
    }

    Error errorResponse;

    if (exception instanceof ApiException apiException) {

      errorResponse = apiException.getError();

    } else {
      errorResponse = RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
          Details.builder().message(
              "An error occurred for request " + request.getRequestId() + ": "
                  + exception.getMessage()).build());
    }

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
