package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.openapitools.client.model.Relay;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoolRegistration {

  private String txHash;
  private int certIndex;
  private String poolId;
  private String vrfKeyHash;
  private String pledge;
  private String margin;
  private String cost;
  private String rewardAccount;
  private Set<String> owners;
  private List<Relay> relays;

}
