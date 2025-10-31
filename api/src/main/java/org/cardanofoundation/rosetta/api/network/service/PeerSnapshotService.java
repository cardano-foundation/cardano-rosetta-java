package org.cardanofoundation.rosetta.api.network.service;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.openapitools.client.model.Peer;

/**
 * Service for loading peers from Cardano Node Genesis mode peer snapshot files.
 * This is used with Cardano Node 10.5.1+ which uses Ouroboros Genesis consensus.
 */
public interface PeerSnapshotService {

  /**
   * Load peers from a peer snapshot file.
   * The file path is resolved relative to the provided base directory.
   *
   * @param peerSnapshotFile the peer snapshot file name (e.g., "peer-snapshot.json")
   * @param baseDirectory the base directory to resolve the file path from
   * @return list of peers extracted from the snapshot, or empty list if file doesn't exist or parsing fails
   */
  List<Peer> loadPeersFromSnapshot(@NotNull String peerSnapshotFile, @NotNull String baseDirectory);
}
