package org.cardanofoundation.rosetta.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> handleGlobalException(Exception exception,
      HttpServletRequest request) {
    log.error("An error occurred during the request", exception);

    Error errorResponse = RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
        new Details("An error occurred for request " + request.getRequestId() + ": "
            + exception.getMessage()));

    return new ResponseEntity<>(errorResponse, resolveHttpStatus(errorResponse));
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Error> handleApiException(ApiException apiException) {
    log.error("An API exception has raised", apiException);
    return new ResponseEntity<>(apiException.getError(), resolveHttpStatus(apiException.getError()));
  }

  @ExceptionHandler(CompletionException.class)
  public ResponseEntity<Error> handleCompletionException(CompletionException exception, HttpServletRequest request) {
    Throwable cause = exception.getCause();
    if (cause instanceof ApiException apiException) {
      log.error("An API exception has raised", apiException);
      return new ResponseEntity<>(apiException.getError(), resolveHttpStatus(apiException.getError()));
    }
    return handleGlobalException(exception, request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Error> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException methodArgumentNotValidException, HttpServletRequest request) {
    log.error("An error occurred during the validation", methodArgumentNotValidException);

    List<String> errors = methodArgumentNotValidException.getBindingResult().getFieldErrors()
        .stream()
        .map(error -> error.getField() + " " + error.getDefaultMessage())
        .toList();
    Error errorResponse = RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(false,
        new Details("An error occurred for request " + request.getRequestId() + ": "
            + errors));

    return new ResponseEntity<>(errorResponse, resolveHttpStatus(errorResponse));
  }

  private static HttpStatus resolveHttpStatus(Error error) {
    if (error.isRetriable()) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return HttpStatus.BAD_REQUEST;
  }
}
