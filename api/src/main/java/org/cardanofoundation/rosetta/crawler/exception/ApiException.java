package org.cardanofoundation.rosetta.crawler.exception;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
public class ApiException extends RuntimeException {

  private Error error;

  public ApiException(Error error) {
    super();
    this.error = error;
  }

}
