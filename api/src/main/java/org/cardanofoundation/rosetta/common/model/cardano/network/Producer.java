package org.cardanofoundation.rosetta.common.model.cardano.network;

import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Producer {
  private String addr;
  @Nullable
  private Integer port;
  @Nullable
  private Integer valency;
}
