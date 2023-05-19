package org.cardanofoundation.rosetta.crawler.projection.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.crawler.model.rest.TokenBundleItem;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.PublicKey;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationMetadata {

  private Amount withdrawalAmount;
  private Amount depositAmount;
  private Amount refundAmount;
  private PublicKey stakingCredential;
  private String poolKeyHash;
  private Long epoch;
  private List<TokenBundleItem> tokenBundle;
  private String poolRegistrationCert;
  private PoolRegistrationParams poolRegistrationParams;
  private VoteRegistrationMetadata voteRegistrationMetadata;
}

