package org.cardanofoundation.rosetta.common.exception;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:56
 */
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Error implements Serializable {

  @Serial
  private static final long serialVersionUID = -217256584923275221L;
  private int code;
  private String message;
  private boolean retriable;
  private String description;
  private Details details;
}
