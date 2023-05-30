package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PoolRegistrationParams
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PoolRegistrationParams {

  @JsonProperty("vrfKeyHash")
  private String vrfKeyHash;

  @JsonProperty("rewardAddress")
  private String rewardAddress;

  @JsonProperty("pledge")
  private String pledge;

  @JsonProperty("cost")
  private String cost;

  @JsonProperty("poolOwners")
  @Valid
  private List<String> poolOwners = new ArrayList<>();

  @JsonProperty("relays")
  @Valid
  private List<Relay> relays = new ArrayList<>();

  @JsonProperty("margin")
  private PoolMargin margin;

  @JsonProperty("margin_percentage")
  private String marginPercentage;

  @JsonProperty("poolMetadata")
  private PoolMetadata poolMetadata;


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PoolRegistrationParams {\n");
    sb.append("    vrfKeyHash: ").append(toIndentedString(vrfKeyHash)).append("\n");
    sb.append("    rewardAddress: ").append(toIndentedString(rewardAddress)).append("\n");
    sb.append("    pledge: ").append(toIndentedString(pledge)).append("\n");
    sb.append("    cost: ").append(toIndentedString(cost)).append("\n");
    sb.append("    poolOwners: ").append(toIndentedString(poolOwners)).append("\n");
    sb.append("    relays: ").append(toIndentedString(relays)).append("\n");
    sb.append("    margin: ").append(toIndentedString(margin)).append("\n");
    sb.append("    marginPercentage: ").append(toIndentedString(marginPercentage)).append("\n");
    sb.append("    poolMetadata: ").append(toIndentedString(poolMetadata)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

