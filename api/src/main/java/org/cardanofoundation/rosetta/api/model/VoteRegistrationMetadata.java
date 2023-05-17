package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Catalyst registration metadata format
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VoteRegistrationMetadata {

  @JsonProperty("stakeKey")
  private PublicKey stakeKey;

  @JsonProperty("votingKey")
  private PublicKey votingKey;

  @JsonProperty("rewardAddress")
  private String rewardAddress;

  @JsonProperty("votingNonce")
  private Integer votingNonce;

  @JsonProperty("votingSignature")
  private String votingSignature;


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VoteRegistrationMetadata {\n");
    sb.append("    stakeKey: ").append(toIndentedString(stakeKey)).append("\n");
    sb.append("    votingKey: ").append(toIndentedString(votingKey)).append("\n");
    sb.append("    rewardAddress: ").append(toIndentedString(rewardAddress)).append("\n");
    sb.append("    votingNonce: ").append(toIndentedString(votingNonce)).append("\n");
    sb.append("    votingSignature: ").append(toIndentedString(votingSignature)).append("\n");
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

