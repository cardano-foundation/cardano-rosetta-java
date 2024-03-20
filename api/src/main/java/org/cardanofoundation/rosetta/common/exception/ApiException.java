package org.cardanofoundation.rosetta.common.exception;

import java.io.Serial;

import lombok.Data;

@Data
public class ApiException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 7966468689369382174L;
  private final Error error;

  public ApiException(Error error) {
    super();
    this.error = error;
  }

  public ApiException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
