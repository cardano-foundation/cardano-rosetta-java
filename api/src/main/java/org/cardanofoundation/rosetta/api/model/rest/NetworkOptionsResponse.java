package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.client.model.Allow;
import org.openapitools.client.model.Version;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:34
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkOptionsResponse {
  @JsonProperty("version")
  private Version version;
  @JsonProperty("allow")
  private Allow allow;
}
