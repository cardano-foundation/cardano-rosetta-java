package org.cardanofoundation.rosetta.api.projection.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PoolRegistrationParams {

  private String vrfKeyHash;
  private String rewardAddress;
  private String pledge;
  private String cost;
  private List<String> poolOwners;
  private List<PoolRelay> relays;
  private PoolMargin margin;
  private String marginPercentage;
  private PoolMetadata poolMetadata;
}