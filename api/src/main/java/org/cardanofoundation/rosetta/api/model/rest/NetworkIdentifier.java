package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.openapitools.client.model.SubNetworkIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
