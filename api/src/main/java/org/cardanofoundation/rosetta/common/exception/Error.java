package org.cardanofoundation.rosetta.common.exception;

import java.io.Serial;
import java.io.Serializable;

import lombok.*;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:56
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Error implements Serializable {

  @Serial
  private static final long serialVersionUID = -217256584923275221L;
  private int code;
  private String message;
  private boolean retriable;
  private String description;
  private Details details;
}
