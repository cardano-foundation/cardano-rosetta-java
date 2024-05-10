package org.cardanofoundation.rosetta.common.exception;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

@ControllerAdvice
public class GlobalExceptionHandler {

  @Value("${api.exception.isPrintStackTrace:true}")
  private String isPrintStackTrace;


  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> handleGlobalException(Exception exception,
      HttpServletRequest request) {
    printStackTraceIfNeeded(exception);
    Error errorResponse = RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
        new Details("An error occurred for request " + request.getRequestId() + ": "
                + exception.getMessage()));

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Error> handleApiException(ApiException apiException) {
    printStackTraceIfNeeded(apiException);
    return new ResponseEntity<>(apiException.getError(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Error> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException methodArgumentNotValidException, HttpServletRequest request) {
    printStackTraceIfNeeded(methodArgumentNotValidException);
    List<String> errors = methodArgumentNotValidException.getBindingResult().getFieldErrors()
        .stream().map(FieldError::getDefaultMessage).toList();
    Error errorResponse = RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
        new Details("An error occurred for request " + request.getRequestId() + ": "
                + errors));
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private void printStackTraceIfNeeded(Exception exception) {
    if (isPrintStackTrace.equals("true")) {
      exception.printStackTrace();
    }
  }
}
