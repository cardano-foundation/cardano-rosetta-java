package org.cardanofoundation.rosetta.common.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiException extends RuntimeException {

  private Error error;

  public ApiException(Error error) {
    super();
    this.error = error;
  }

}
