package org.cardanofoundation.rosetta.common.util;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.StructuredTaskScope;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType.TIMEOUT;
import static org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType.UNSPECIFIED_ERROR;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StructuredTaskScopeUtilTest {

  @Test
  void join_returnsNormally_whenAllSubtasksSucceed() {
    try (var scope = openScope(Duration.ofSeconds(5))) {
      StructuredTaskScope.Subtask<String> subtask = scope.fork(() -> "ok");

      StructuredTaskScopeUtil.join(scope, "test data");

      assertThat(subtask.get()).isEqualTo("ok");
    }
  }

  @Test
  void join_throwsUnspecifiedError_whenSubtaskFails() {
    try (var scope = openScope(Duration.ofSeconds(5))) {
      scope.fork(() -> {
        throw new IllegalStateException("db down");
      });

      ApiException exception = assertThrows(ApiException.class, () -> StructuredTaskScopeUtil.join(scope, "test data"));

      assertThat(exception.getError().getCode()).isEqualTo(UNSPECIFIED_ERROR.getCode());
    }
  }

  @Test
  void join_throwsTimeoutError_whenSubtasksExceedTimeout() {
    try (var scope = openScope(Duration.ofSeconds(1))) {
      scope.fork(() -> {
        new CountDownLatch(1).await(); // blocks until the scope times out and interrupts it
        return null;
      });

      ApiException exception = assertThrows(ApiException.class, () -> StructuredTaskScopeUtil.join(scope, "test data"));

      assertThat(exception.getError().getCode()).isEqualTo(TIMEOUT.getCode());
      assertThat(exception.getError().isRetriable()).isTrue();
    }
  }

  @Test
  void join_throwsUnspecifiedErrorAndKeepsInterruptFlag_whenInterrupted() {
    try (var scope = openScope(Duration.ofSeconds(5))) {
      scope.fork(() -> {
        new CountDownLatch(1).await(); // blocks until the scope is closed and interrupts it
        return null;
      });

      Thread.currentThread().interrupt();
      ApiException exception = assertThrows(ApiException.class, () -> StructuredTaskScopeUtil.join(scope, "test data"));

      assertThat(exception.getError().getCode()).isEqualTo(UNSPECIFIED_ERROR.getCode());
      assertThat(Thread.interrupted()).isTrue(); // also clears the flag for the following tests
    }
  }

  private static StructuredTaskScope<Object, Void> openScope(Duration timeout) {
    return StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow(), cf -> cf.withTimeout(timeout));
  }

}
