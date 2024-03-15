package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class BlockVersion {

  private short major;
  private short minor;
  private byte alt;
}
