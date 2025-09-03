package org.cardanofoundation.rosetta.common.exception;

import java.util.Collections;
import java.util.concurrent.CompletionException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  private static final String TEST_ID = "1";
  private static final Integer CARDANO_ERROR_CODE = 5000;
  private static final String FIELD_NAME = "fieldName";
  private static final String MISSING_FIELD_MESSAGE = "must be not null";
  private static final String ARGUMENT_NOT_VALID_MESSAGE = FIELD_NAME + " " + MISSING_FIELD_MESSAGE;
  private static final String GLOBAL_MESSAGE = "Global Exception";
  private static final String API_MESSAGE = "API Exception";
  private static final String GLOBAL_EXCEPTION_MESSAGE =
      "An error occurred for request " + TEST_ID + ": " + GLOBAL_MESSAGE;
  private static final String COMPLETION_GLOBAL_EXCEPTION_MESSAGE =
      "An error occurred for request " + TEST_ID + ": " + "java.lang.Exception: " + GLOBAL_MESSAGE;
  private static final String API_EXCEPTION_MESSAGE =
      "An error occurred for request " + TEST_ID + ": " + API_MESSAGE;
  private static final String ARGUMENT_NOT_VALID_EXCEPTION_MESSAGE =
      "An error occurred for request " + TEST_ID + ": [" + ARGUMENT_NOT_VALID_MESSAGE + "]";

  @Mock
  private HttpServletRequest request;

  @Mock
  private MethodArgumentNotValidException methodArgumentNotValidException;

  @Mock
  private FieldError fieldError;

  @Mock
  private BindingResult bindingResult;

  @InjectMocks
  private GlobalExceptionHandler underTest;

  @Test
  void handleGlobalException_shouldReturnInternalServerError() {
    Exception globalException = new Exception(GLOBAL_MESSAGE);

    when(request.getRequestId()).thenReturn(TEST_ID);
    ResponseEntity<Error> result = underTest.handleGlobalException(globalException, request);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(GLOBAL_EXCEPTION_MESSAGE, result.getBody().getDetails().getMessage());
    assertEquals(CARDANO_ERROR_CODE, result.getBody().getCode());
  }

  @Test
  void handleAPIException_shouldReturnInternalServerError() {
    ApiException apiException = createApiException();

    ResponseEntity<Error> result = underTest.handleApiException(apiException);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(apiException.getError().getDetails().getMessage(),
        result.getBody().getDetails().getMessage());
    assertEquals(apiException.getError().getCode(), result.getBody().getCode());
  }

  @Test
  void handleCompletionException_whenAPIExceptionWrapped_shouldReturnInternalServerError() {
    ApiException apiException = createApiException();
    CompletionException completionException = new CompletionException(apiException);

    ResponseEntity<Error> result = underTest.handleCompletionException(completionException,
        request);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(apiException.getError().getDetails().getMessage(),
        result.getBody().getDetails().getMessage());
    assertEquals(apiException.getError().getCode(), result.getBody().getCode());
  }

  @Test
  void handleCompletionException_whenGlobalExceptionWrapped_shouldReturnInternalServerError() {
    Exception globalException = new Exception(GLOBAL_MESSAGE);
    CompletionException completionException = new CompletionException(globalException);

    when(request.getRequestId()).thenReturn(TEST_ID);

    ResponseEntity<Error> result = underTest.handleCompletionException(completionException,
        request);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(COMPLETION_GLOBAL_EXCEPTION_MESSAGE, result.getBody().getDetails().getMessage());
    assertEquals(CARDANO_ERROR_CODE, result.getBody().getCode());
  }

  @Test
  void handleMethodArgumentNotValidException_shouldReturnBadRequest() {
    when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
    when(fieldError.getField()).thenReturn(FIELD_NAME);
    when(fieldError.getDefaultMessage()).thenReturn(MISSING_FIELD_MESSAGE);
    when(request.getRequestId()).thenReturn(TEST_ID);

    ResponseEntity<Error> result = underTest.handleMethodArgumentNotValidException(
        methodArgumentNotValidException, request);

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertNotNull(result.getBody());
    assertEquals(ARGUMENT_NOT_VALID_EXCEPTION_MESSAGE, result.getBody().getDetails().getMessage());
    assertEquals(CARDANO_ERROR_CODE, result.getBody().getCode());
  }

  private ApiException createApiException() {
    return new ApiException(RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(true,
        new Details(API_EXCEPTION_MESSAGE)));
  }
}
