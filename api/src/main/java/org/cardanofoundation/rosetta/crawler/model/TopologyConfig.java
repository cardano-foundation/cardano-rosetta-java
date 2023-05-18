package org.cardanofoundation.rosetta.crawler.model;


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
  private List<Producer> producers;
  @Nullable
  private List<PublicRoot> publicRoots;
}
