package org.cardanofoundation.rosetta.common.util;

import java.util.concurrent.StructuredTaskScope;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

/**
 * Shared join + error mapping for the parallel DB queries run in a {@link StructuredTaskScope}.
 */
@Slf4j
@UtilityClass
public class StructuredTaskScopeUtil {

  /**
   * Waits for all subtasks and maps scope failures to Rosetta errors.
   * <p>
   * In the JDK 25 API a timeout ({@code withTimeout(...)}) is reported as the unchecked
   * {@link StructuredTaskScope.TimeoutException}, so the compiler does not force a catch for it:
   * it must be mapped here explicitly to the Rosetta TIMEOUT error.
   *
   * @param scope       the scope whose subtasks have been forked
   * @param description what is being fetched, used in log and error messages (e.g. "transaction data")
   */
  public static void join(StructuredTaskScope<?, ?> scope, String description) {
    try {
      scope.join();
    } catch (StructuredTaskScope.FailedException e) {
      log.error("Error fetching {}", description, e);
      throw ExceptionFactory.unspecifiedError("Error fetching %s, msg:%s".formatted(description, e.getMessage()));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Error fetching {}", description, e);
      throw ExceptionFactory.unspecifiedError("Error fetching %s, msg:%s".formatted(description, e.getMessage()));
    } catch (StructuredTaskScope.TimeoutException e) {
      log.error("Timeout fetching {}", description, e);
      throw ExceptionFactory.timeOut("timeout while fetching %s from db.".formatted(description));
    }
  }

}
