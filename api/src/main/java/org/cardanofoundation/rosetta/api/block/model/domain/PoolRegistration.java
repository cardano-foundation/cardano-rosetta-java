package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

import org.cardanofoundation.rosetta.api.block.model.entity.PoolRegistrationEnity;
import org.openapitools.client.model.Relay;

@Data
@Builder
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

  public static PoolRegistration fromEntity(PoolRegistrationEnity entity) {
    return PoolRegistration.builder()
        .txHash(entity.getTxHash())
        .certIndex(entity.getCertIndex())
        .poolId(entity.getPoolId())
        .vrfKeyHash(entity.getVrfKeyHash())
        .pledge(entity.getPledge().toString())
        .margin(entity.getMargin().toString())
        .cost(entity.getCost().toString())
        .rewardAccount(entity.getRewardAccount())
        .relays(entity.getRelays().stream().map(
            relay -> new Relay(relay.getIpv4(), relay.getIpv6(), relay.getDnsName(),
                relay.getPort(), "")).toList()) // TODO check type
        .owners(entity.getPoolOwners())
        .build();
  }
}