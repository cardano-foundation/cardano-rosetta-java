package org.cardanofoundation.rosetta.common.exception;

import java.io.Serial;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 7966468689369382174L;
  private final Error error;

  public ApiException(Error error) {
    super();
    this.error = error;
  }
}
