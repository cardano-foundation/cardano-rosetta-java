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
public class PoolRegistration {

  private String vrfKeyHash;
  private String pledge;
  private String margin;
  private String cost;
  private String address;
  private String poolHash;
  private List<String> owners;
  private List<PoolRelay> relays;
  private String metadataUrl;
  private String metadataHash;
}