package org.cardanofoundation.rosetta.common.model.cardano.network;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TopologyConfig {
  @JsonProperty("Producers")
  private List<Producer> producers;
  @JsonProperty("publicRoots")
  private List<PublicRoot> publicRoots;
}
