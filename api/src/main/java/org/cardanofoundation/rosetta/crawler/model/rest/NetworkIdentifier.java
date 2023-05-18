package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import lombok.*;
import org.openapitools.client.model.SubNetworkIdentifier;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 15:08
 */
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkIdentifier {
  @JsonProperty("blockchain")
  private String blockchain;

  @JsonProperty("network")
  private String network;

  @JsonProperty("sub_network_identifier")
  @Nullable
  private SubNetworkIdentifier subNetworkIdentifier;
}
