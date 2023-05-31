package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockRequest {

  @JsonProperty("network_identifier")
  private NetworkIdentifier networkIdentifier;

  @JsonProperty("block_identifier")
  private PartialBlockIdentifier blockIdentifier;


}
