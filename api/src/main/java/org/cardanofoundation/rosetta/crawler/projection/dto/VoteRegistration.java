package org.cardanofoundation.rosetta.crawler.projection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteRegistration {

  private String votingKey;
  private String stakeKey;
  private String rewardAddress;
  private Long votingNonce;
  private String votingSignature;
}