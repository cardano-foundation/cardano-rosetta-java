package org.cardanofoundation.rosetta.api.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopologyConfig {
  @Nullable
  @JsonProperty("Producers")
  private List<Producer> producers;
  @Nullable
  @JsonProperty("publicRoots")
  private List<PublicRoot> publicRoots;
}
