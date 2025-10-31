package org.cardanofoundation.rosetta.api.network.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.network.model.PeerSnapshotConfig;
import org.cardanofoundation.rosetta.api.network.model.Relay;
import org.cardanofoundation.rosetta.common.util.FileUtils;
import org.openapitools.client.model.Peer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of PeerSnapshotService for loading peers from Genesis mode peer snapshot files.
 * Cardano Node 10.5.1+ uses Ouroboros Genesis which requires peer snapshots instead of bootstrap peers.
 */
@Service
@Slf4j
public class PeerSnapshotServiceImpl implements PeerSnapshotService {

  @Override
  public List<Peer> loadPeersFromSnapshot(@NotNull String peerSnapshotFile, @NotNull String baseDirectory) {
    try {
      Path snapshotPath = resolveSnapshotPath(peerSnapshotFile, baseDirectory);
      String snapshotFilePath = snapshotPath.toString();

      log.debug("[loadPeersFromSnapshot] Loading peer snapshot from: {}", snapshotFilePath);

      if (!new File(snapshotFilePath).exists()) {
        log.warn("[loadPeersFromSnapshot] Peer snapshot file not found: {}", snapshotFilePath);
        return new ArrayList<>();
      }

      PeerSnapshotConfig peerSnapshot = parsePeerSnapshot(snapshotFilePath);

      if (peerSnapshot == null || peerSnapshot.getBigLedgerPools() == null) {
        log.warn("[loadPeersFromSnapshot] Peer snapshot is empty or invalid");
        return new ArrayList<>();
      }

      List<Peer> peers = extractPeersFromSnapshot(peerSnapshot);

      log.info("[loadPeersFromSnapshot] Extracted {} relays from {} big ledger pools",
          peers.size(), peerSnapshot.getBigLedgerPools().size());

      return peers;

    } catch (IOException e) {
      log.error("[loadPeersFromSnapshot] Failed to load peer snapshot from: {}", peerSnapshotFile, e);
      return new ArrayList<>();
    }
  }

  private Path resolveSnapshotPath(String peerSnapshotFile, String baseDirectory) {
    Path basePath = Paths.get(baseDirectory);
    return basePath.resolve(peerSnapshotFile);
  }

  private PeerSnapshotConfig parsePeerSnapshot(String snapshotFilePath) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    String content = FileUtils.fileReader(snapshotFilePath);
    return mapper.readValue(content, PeerSnapshotConfig.class);
  }

  private List<Peer> extractPeersFromSnapshot(PeerSnapshotConfig peerSnapshot) {
    return peerSnapshot.getBigLedgerPools().stream()
        .flatMap(pool -> pool.getRelays().stream())
        .map(this::mapRelayToPeer)
        .toList();
  }

  @NotNull
  private Peer mapRelayToPeer(@NotNull Relay relay) {
    String address;
    String type;

    if (relay.getDomain() != null) {
      address = relay.getDomain();
      type = "domain";
    } else {
      address = relay.getAddress();
      type = isIpv6(address) ? "IPv6" : "IPv4";
    }

    address = "%s:%d".formatted(address, relay.getPort());

    return new Peer(address, Map.of("type", type));
  }

  private boolean isIpv6(String address) {
    return address.contains(":");
  }
}
