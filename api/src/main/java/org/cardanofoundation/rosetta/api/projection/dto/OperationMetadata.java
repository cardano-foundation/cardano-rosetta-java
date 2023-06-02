package org.cardanofoundation.rosetta.api.projection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.rest.TokenBundleItem;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.PublicKey;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
  @JsonProperty("tokenBundle")
  private List<TokenBundleItem> tokenBundle;
  @JsonProperty("poolRegistrationCert")
  private String poolRegistrationCert;

  @JsonProperty("poolRegistrationParams")
  private PoolRegistrationParams poolRegistrationParams;
  @JsonProperty("voteRegistrationMetadata")
  private VoteRegistrationMetadata voteRegistrationMetadata;
}

