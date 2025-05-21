package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.openapitools.client.model.Peer;
import org.openapitools.client.model.SyncStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkStatus {

  private BlockIdentifierExtended latestBlock;
  private BlockIdentifierExtended oldestBlock;
  private BlockIdentifierExtended genesisBlock;
  private SyncStatus syncStatus;
  private List<Peer> peers;

}
