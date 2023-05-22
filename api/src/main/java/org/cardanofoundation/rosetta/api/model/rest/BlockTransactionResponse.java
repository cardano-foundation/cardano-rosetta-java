package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.client.model.Transaction;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockTransactionResponse {
  @JsonProperty("transaction")
  private Transaction transaction;
}
