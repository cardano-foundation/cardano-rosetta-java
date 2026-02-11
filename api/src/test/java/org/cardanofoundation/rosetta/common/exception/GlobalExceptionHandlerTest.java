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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.assertj.core.api.Assertions.assertThat;
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

  @Nested
  class HandleGlobalException {

    @Test
    void shouldReturnInternalServerError() {
      Exception globalException = new Exception(GLOBAL_MESSAGE);

      when(request.getRequestId()).thenReturn(TEST_ID);
      ResponseEntity<Error> result = underTest.handleGlobalException(globalException, request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getDetails().getMessage()).isEqualTo(GLOBAL_EXCEPTION_MESSAGE);
      assertThat(result.getBody().getCode()).isEqualTo(CARDANO_ERROR_CODE);
      assertThat(result.getBody().isRetriable()).isFalse();
    }
  }

  @Nested
  class HandleApiException {

    @Test
    void with5xxxCode_shouldReturnInternalServerError() {
      ApiException apiException = create5xxxApiException();

      ResponseEntity<Error> result = underTest.handleApiException(apiException);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getDetails().getMessage())
          .isEqualTo(apiException.getError().getDetails().getMessage());
      assertThat(result.getBody().getCode()).isEqualTo(apiException.getError().getCode());
      assertThat(result.getBody().isRetriable()).isFalse();
    }

    @Test
    void with4xxxCode_shouldReturnBadRequest() {
      ApiException apiException = new ApiException(
          RosettaErrorType.BLOCK_NOT_FOUND.toRosettaError());

      ResponseEntity<Error> result = underTest.handleApiException(apiException);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getCode()).isEqualTo(4001);
      assertThat(result.getBody().isRetriable()).isTrue();
    }
  }

  @Nested
  class HandleCompletionException {

    @Test
    void whenAPIExceptionWrapped_shouldReturnInternalServerError() {
      ApiException apiException = create5xxxApiException();
      CompletionException completionException = new CompletionException(apiException);

      ResponseEntity<Error> result = underTest.handleCompletionException(completionException,
          request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getDetails().getMessage())
          .isEqualTo(apiException.getError().getDetails().getMessage());
      assertThat(result.getBody().getCode()).isEqualTo(apiException.getError().getCode());
    }

    @Test
    void when4xxxAPIExceptionWrapped_shouldReturnBadRequest() {
      ApiException apiException = new ApiException(
          RosettaErrorType.BLOCK_NOT_FOUND.toRosettaError());
      CompletionException completionException = new CompletionException(apiException);

      ResponseEntity<Error> result = underTest.handleCompletionException(completionException,
          request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getCode()).isEqualTo(4001);
      assertThat(result.getBody().isRetriable()).isTrue();
    }

    @Test
    void whenGlobalExceptionWrapped_shouldReturnInternalServerError() {
      Exception globalException = new Exception(GLOBAL_MESSAGE);
      CompletionException completionException = new CompletionException(globalException);

      when(request.getRequestId()).thenReturn(TEST_ID);

      ResponseEntity<Error> result = underTest.handleCompletionException(completionException,
          request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getDetails().getMessage())
          .isEqualTo(COMPLETION_GLOBAL_EXCEPTION_MESSAGE);
      assertThat(result.getBody().getCode()).isEqualTo(CARDANO_ERROR_CODE);
    }
  }

  @Nested
  class HandleMethodArgumentNotValidException {

    @Test
    void shouldReturnBadRequest() {
      when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
      when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
      when(fieldError.getField()).thenReturn(FIELD_NAME);
      when(fieldError.getDefaultMessage()).thenReturn(MISSING_FIELD_MESSAGE);
      when(request.getRequestId()).thenReturn(TEST_ID);

      ResponseEntity<Error> result = underTest.handleMethodArgumentNotValidException(
          methodArgumentNotValidException, request);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(result.getBody()).isNotNull();
      assertThat(result.getBody().getDetails().getMessage())
          .isEqualTo(ARGUMENT_NOT_VALID_EXCEPTION_MESSAGE);
      assertThat(result.getBody().getCode()).isEqualTo(CARDANO_ERROR_CODE);
    }
  }

  private ApiException create5xxxApiException() {
    return new ApiException(RosettaErrorType.UNSPECIFIED_ERROR.toRosettaError(
        new Details(API_MESSAGE)));
  }
}
