package org.cardanofoundation.rosetta.common.exception;

import java.io.Serial;

import javax.annotation.Nullable;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 7966468689369382174L;
  private final Error error;
  @Nullable
  private final HttpStatus httpStatus;

  public ApiException(Error error) {
    this(error, null);
  }

  public ApiException(Error error, @Nullable HttpStatus httpStatus) {
    super(error.getMessage() == null ? "?" : error.getMessage());
    this.error = error;
    this.httpStatus = httpStatus;
  }

}
