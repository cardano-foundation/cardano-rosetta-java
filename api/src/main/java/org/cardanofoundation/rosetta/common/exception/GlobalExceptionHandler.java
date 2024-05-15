package org.cardanofoundation.rosetta.common.exception;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> handleGlobalException(Exception exception,
      HttpServletRequest request) {

    Error errorResponse = RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
        new Details("An error occurred for request " + request.getRequestId() + ": "
            + exception.getMessage()));

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Error> handleApiException(ApiException apiException) {
    return new ResponseEntity<>(apiException.getError(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Error> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException methodArgumentNotValidException, HttpServletRequest request) {

    List<String> errors = methodArgumentNotValidException.getBindingResult().getFieldErrors()
        .stream()
        .map(error -> error.getField() + " " + error.getDefaultMessage())
        .toList();
    Error errorResponse = RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
        new Details("An error occurred for request " + request.getRequestId() + ": "
            + errors));
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
