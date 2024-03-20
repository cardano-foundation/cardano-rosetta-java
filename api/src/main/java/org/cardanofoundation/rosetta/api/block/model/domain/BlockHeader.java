package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.bloxbean.cardano.yaci.core.model.HeaderBody;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockHeader {

  private HeaderBody headerBody;
  private String bodySignature;

  @Override
  public String toString() {
    return "BlockHeader{" +
        "headerBody=" + headerBody +
        '}';
  }
}
