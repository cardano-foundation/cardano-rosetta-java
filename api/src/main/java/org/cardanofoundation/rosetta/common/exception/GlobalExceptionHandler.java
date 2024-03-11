package org.cardanofoundation.rosetta.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

  @Value("${api.exception.isPrintStackTrace:true}")
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

    } else if ( exception instanceof MethodArgumentNotValidException methodArgumentNotValidException){
      List<String> errors = methodArgumentNotValidException.getBindingResult().getFieldErrors()
          .stream().map(FieldError::getDefaultMessage).toList();
      errorResponse = RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
          Details.builder().message(
              "An error occurred for request " + request.getRequestId() + ": "
                  + errors ).build());
    }
    else {
      errorResponse = RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
          Details.builder().message(
              "An error occurred for request " + request.getRequestId() + ": "
                  + exception.getMessage()).build());
    }

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
