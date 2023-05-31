package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.client.model.Block;
import org.openapitools.client.model.TransactionIdentifier;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:55
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockResponse {

  @JsonProperty("block")
  private Block block;
  @JsonProperty("other_transactions")
  private List<TransactionIdentifier> otherTransactions;


}
