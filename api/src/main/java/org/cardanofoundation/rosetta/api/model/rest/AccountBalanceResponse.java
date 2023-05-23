package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import org.openapitools.client.model.Amount;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:36
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBalanceResponse {

  @JsonProperty("block_identifier")
  BlockIdentifier blockIdentifier;

  @JsonProperty("balances")
  List<Amount> balances;

  @JsonProperty("metadata")
  Object metadata;

}
