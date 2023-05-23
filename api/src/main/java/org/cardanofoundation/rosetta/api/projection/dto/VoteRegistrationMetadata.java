package org.cardanofoundation.rosetta.api.projection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.client.model.PublicKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteRegistrationMetadata {

  private PublicKey stakeKey;
  private PublicKey votingKey;
  private String rewardAddress;
  private Long votingNonce;
  private String votingSignature;
}
