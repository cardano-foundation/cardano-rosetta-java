package org.cardanofoundation.rosetta.common.model.cardano;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.rosetta.api.block.model.dto.PoolRegistrationParams;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.PublicKey;

/**
 * Metadata related to Cardano operations
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperationMetadata {

  @JsonProperty("withdrawalAmount")
  private Amount withdrawalAmount;

  @JsonProperty("depositAmount")
  private Amount depositAmount;

  @JsonProperty("refundAmount")
  private Amount refundAmount;

  @JsonProperty("staking_credential")
  private PublicKey stakingCredential;

  @JsonProperty("pool_key_hash")
  private String poolKeyHash;

  @JsonProperty("epoch")
  private Long epoch;

  public OperationMetadata(VoteRegistrationMetadata voteRegistrationMetadata) {
    this.voteRegistrationMetadata = voteRegistrationMetadata;
  }

  @JsonProperty("tokenBundle")
  @Valid
  private List<TokenBundleItem> tokenBundle = null;

  @JsonProperty("poolRegistrationCert")
  private String poolRegistrationCert;

  @JsonProperty("poolRegistrationParams")
  private PoolRegistrationParams poolRegistrationParams;

  @JsonProperty("voteRegistrationMetadata")
  private VoteRegistrationMetadata voteRegistrationMetadata;

  public OperationMetadata(List<TokenBundleItem> tokenBundle) {
    this.tokenBundle=tokenBundle;
  }

  public OperationMetadata(PublicKey stakingCredential) {
    this.stakingCredential=stakingCredential;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationMetadata {\n");
    sb.append("    withdrawalAmount: ").append(toIndentedString(withdrawalAmount)).append("\n");
    sb.append("    depositAmount: ").append(toIndentedString(depositAmount)).append("\n");
    sb.append("    refundAmount: ").append(toIndentedString(refundAmount)).append("\n");
    sb.append("    stakingCredential: ").append(toIndentedString(stakingCredential)).append("\n");
    sb.append("    poolKeyHash: ").append(toIndentedString(poolKeyHash)).append("\n");
    sb.append("    epoch: ").append(toIndentedString(epoch)).append("\n");
    sb.append("    tokenBundle: ").append(toIndentedString(tokenBundle)).append("\n");
    sb.append("    poolRegistrationCert: ").append(toIndentedString(poolRegistrationCert)).append("\n");
    sb.append("    poolRegistrationParams: ").append(toIndentedString(poolRegistrationParams)).append("\n");
    sb.append("    voteRegistrationMetadata: ").append(toIndentedString(voteRegistrationMetadata)).append("\n");
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

  public void addTokenBundleItem(TokenBundleItem tokenBundleItem) {
    if(this.tokenBundle == null) {
      this.tokenBundle = new ArrayList<>();
    }
    this.tokenBundle.add(tokenBundleItem);
  }
}

