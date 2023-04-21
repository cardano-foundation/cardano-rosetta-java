package org.cardanofoundation.rosetta.consumer.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockMessage {

  private String name;
  private Instant createdDate = Instant.now();
}
