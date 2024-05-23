package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.openapitools.client.model.Peer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkStatus {

  private BlockIdentifierExtended latestBlock;
  private BlockIdentifierExtended genesisBlock;
  private List<Peer> peers;
}
