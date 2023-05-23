package org.cardanofoundation.rosetta.api.model.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:54
 */
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationStatus {
  private String status;
  private boolean successful;
}
