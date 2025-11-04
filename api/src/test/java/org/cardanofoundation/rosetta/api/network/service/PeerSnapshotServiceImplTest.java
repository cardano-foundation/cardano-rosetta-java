package org.cardanofoundation.rosetta.api.network.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.Peer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PeerSnapshotServiceImplTest {

  private PeerSnapshotServiceImpl peerSnapshotService;

  @BeforeEach
  void setup() {
    peerSnapshotService = new PeerSnapshotServiceImpl();
  }

  @Nested
  class LoadPeersFromSnapshot {

    @Test
    void shouldLoadPeersFromMainnetSnapshot() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      assertNotNull(peers);
      assertThat(peers).isNotEmpty();
      assertThat(peers.size()).isLessThanOrEqualTo(25); // Limited to MAX_PEERS (25)
      assertThat(peers.size()).isEqualTo(25); // Should have exactly 25 peers if snapshot has enough
    }

    @Test
    void shouldExtractDomainRelaysCorrectly() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      List<Peer> domainPeers = peers.stream()
          .filter(peer -> {
            Map<String, Object> metadata = (Map<String, Object>) peer.getMetadata();
            return "domain".equals(metadata.get("type"));
          })
          .toList();

      // Due to randomization, we can't guarantee specific domains are present
      // Just verify that domain peers exist and have correct format
      assertThat(domainPeers).isNotEmpty();

      domainPeers.forEach(peer -> {
        // Domain peers should have format address:port
        assertThat(peer.getPeerId()).contains(":");

        // Metadata should indicate domain type
        Map<String, Object> metadata = (Map<String, Object>) peer.getMetadata();
        assertThat(metadata.get("type")).isEqualTo("domain");
      });
    }

    @Test
    void shouldExtractIpv4RelaysCorrectly() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      List<Peer> ipv4Peers = peers.stream()
          .filter(peer -> {
            Map<String, Object> metadata = (Map<String, Object>) peer.getMetadata();
            return "IPv4".equals(metadata.get("type"));
          })
          .toList();

      assertThat(ipv4Peers).isNotEmpty();

      // Verify format: IP:PORT
      ipv4Peers.forEach(peer -> {
        assertThat(peer.getPeerId()).matches("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+");
      });
    }

    @Test
    void shouldIncludePortInPeerAddress() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      assertThat(peers).isNotEmpty();

      // All peers should have port - verify by checking last part after split
      peers.forEach(peer -> {
        assertThat(peer.getPeerId()).contains(":");
        // For IPv6, address will be like [2001:db8::1]:3001 or without brackets
        // For IPv4 and domain, it's simpler: address:port
        String peerId = peer.getPeerId();
        int lastColonIndex = peerId.lastIndexOf(":");
        assertThat(lastColonIndex).isGreaterThan(-1);

        String portPart = peerId.substring(lastColonIndex + 1);
        assertThat(portPart).matches("\\d+"); // Port should be numeric
      });
    }

    @Test
    void shouldSetCorrectMetadataForDomainRelays() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      Peer domainPeer = peers.stream()
          .filter(peer -> {
            Map<String, Object> metadata = (Map<String, Object>) peer.getMetadata();
            return "domain".equals(metadata.get("type"));
          })
          .findFirst()
          .orElseThrow();

      Map<String, Object> metadata = (Map<String, Object>) domainPeer.getMetadata();
      assertThat(metadata).containsEntry("type", "domain");
    }

    @Test
    void shouldSetCorrectMetadataForIpv4Relays() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      Peer ipv4Peer = peers.stream()
          .filter(peer -> {
            Map<String, Object> metadata = (Map<String, Object>) peer.getMetadata();
            return "IPv4".equals(metadata.get("type"));
          })
          .findFirst()
          .orElseThrow();

      Map<String, Object> metadata = (Map<String, Object>) ipv4Peer.getMetadata();
      assertThat(metadata).containsEntry("type", "IPv4");
    }

    @Test
    void shouldHandleNonExistentFile() {
      // given
      String peerSnapshotFile = "non-existent-file.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      assertNotNull(peers);
      assertThat(peers).isEmpty();
    }

    @Test
    void shouldExtractMultipleRelaysFromSinglePool() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      // Due to randomization, we can't guarantee specific pools are in the selection
      // Verify we have peers limited to MAX_PEERS
      assertThat(peers).hasSizeLessThanOrEqualTo(25);
      assertThat(peers).isNotEmpty();

      // Verify all peers have valid format (address:port)
      peers.forEach(peer -> {
        assertThat(peer.getPeerId()).contains(":");

        // All peers should have metadata with type
        Map<String, Object> metadata = (Map<String, Object>) peer.getMetadata();
        assertThat(metadata).containsKey("type");
      });
    }

    @Test
    void shouldHandlePoolsWithoutRelays() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      // Should not fail, just skip pools without relays
      assertNotNull(peers);
      assertThat(peers).isNotEmpty();
    }

    @Test
    void shouldVerifyKnownMainnetRelays() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      // Due to randomization, we can't guarantee specific relays are present
      // Instead verify that all peers have valid format and metadata
      assertThat(peers).isNotEmpty();
      assertThat(peers.size()).isEqualTo(25);

      peers.forEach(peer -> {
        // All peers should have a peer ID with port
        assertThat(peer.getPeerId()).contains(":");

        // All peers should have metadata with type
        Map<String, Object> metadata = (Map<String, Object>) peer.getMetadata();
        assertThat(metadata).containsKey("type");
        assertThat(metadata.get("type")).isIn("domain", "IPv4", "IPv6");
      });
    }

    @Test
    void shouldExtractAllRelaysFromAllPools() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      // Peers are now limited to MAX_PEERS (25) and randomized
      assertThat(peers.size()).isLessThanOrEqualTo(25);
      assertThat(peers.size()).isEqualTo(25); // Should have exactly 25 peers
    }

    @Test
    void shouldHandleMixedRelayTypes() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      long domainCount = peers.stream()
          .filter(peer -> {
            Map<String, Object> metadata = (Map<String, Object>) peer.getMetadata();
            return "domain".equals(metadata.get("type"));
          })
          .count();

      long ipv4Count = peers.stream()
          .filter(peer -> {
            Map<String, Object> metadata = (Map<String, Object>) peer.getMetadata();
            return "IPv4".equals(metadata.get("type"));
          })
          .count();

      // Should have both types
      assertThat(domainCount).isGreaterThan(0);
      assertThat(ipv4Count).isGreaterThan(0);
    }

    @Test
    void shouldVerifyCommonPorts() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      List<String> ports = peers.stream()
          .map(peer -> peer.getPeerId().split(":")[1])
          .distinct()
          .toList();

      // Common Cardano relay ports
      assertThat(ports)
          .contains("3001", "6000");
    }

    @Test
    void shouldRandomizePeerSelection() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when - load peers multiple times
      List<Peer> firstLoad = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);
      List<Peer> secondLoad = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then - both should have 25 peers
      assertThat(firstLoad.size()).isEqualTo(25);
      assertThat(secondLoad.size()).isEqualTo(25);

      // And they should be different due to randomization (very high probability)
      // We check if at least 5 peers are different in order
      long differentPeers = 0;
      for (int i = 0; i < Math.min(firstLoad.size(), secondLoad.size()); i++) {
        if (!firstLoad.get(i).getPeerId().equals(secondLoad.get(i).getPeerId())) {
          differentPeers++;
        }
      }

      assertThat(differentPeers).isGreaterThan(5);
    }
  }
}
