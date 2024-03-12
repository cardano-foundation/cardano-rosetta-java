package org.cardanofoundation.rosetta.common.model.cardano.pool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.block.model.dto.PoolRelay;

import java.util.List;

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