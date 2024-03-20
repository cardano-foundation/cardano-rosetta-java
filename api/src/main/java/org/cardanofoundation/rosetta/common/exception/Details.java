package org.cardanofoundation.rosetta.common.exception;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Details implements Serializable {
  @Serial
  private static final long serialVersionUID = -1003440904096173081L;
  private String message;
}
