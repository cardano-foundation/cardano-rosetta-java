package org.cardanofoundation.rosetta.common.exception;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Details implements Serializable {
  @Serial
  private static final long serialVersionUID = -1003440904096173081L;
  private String message;
}
