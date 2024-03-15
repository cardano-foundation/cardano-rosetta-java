package org.cardanofoundation.rosetta.common.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data //TODO saa: do we need this here?
@NoArgsConstructor
public class ApiException extends RuntimeException {

  private Error error;  //TODO saa strange field.

  public ApiException(Error error) {
    super();
    this.error = error;
  }

  public ApiException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
