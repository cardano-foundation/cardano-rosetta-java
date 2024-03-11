package org.cardanofoundation.rosetta.common.exception;

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
public class Error {
  private int code;
  private String message;
  private boolean retriable;
  private String description;
  private Object details;
}
