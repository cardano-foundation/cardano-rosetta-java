package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 15:08
 */
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkRequest {
  @JsonProperty("network_identifier")
  private NetworkIdentifier networkIdentifier;
}
