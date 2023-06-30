package org.cardanofoundation.rosetta.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
