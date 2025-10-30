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
      assertThat(peers.size()).isGreaterThan(100); // Mainnet has many big ledger pools
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

      assertThat(domainPeers).isNotEmpty();

      // Verify at least one known domain relay
      assertThat(domainPeers)
          .anyMatch(peer -> peer.getPeerId().contains("cardano.figment.io"));
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
      // Nordic pool has multiple relays (Relay1-6.NordicPool.org)
      List<Peer> nordicPoolRelays = peers.stream()
          .filter(peer -> peer.getPeerId().contains("NordicPool.org"))
          .toList();

      assertThat(nordicPoolRelays).isNotEmpty();
      assertThat(nordicPoolRelays.size()).isGreaterThan(1);
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
      List<String> peerIds = peers.stream()
          .map(Peer::getPeerId)
          .toList();

      // Verify some known mainnet relays exist
      assertThat(peerIds)
          .anyMatch(id -> id.contains("cardano.figment.io"))
          .anyMatch(id -> id.contains("NordicPool.org"))
          .anyMatch(id -> id.contains("cardanosuisse.com"));
    }

    @Test
    void shouldExtractAllRelaysFromAllPools() {
      // given
      String peerSnapshotFile = "peer-snapshot.json";
      String baseDirectory = "../config/node/mainnet";

      // when
      List<Peer> peers = peerSnapshotService.loadPeersFromSnapshot(peerSnapshotFile, baseDirectory);

      // then
      // Mainnet snapshot has many pools with multiple relays each
      // Total should be significantly higher than the number of pools
      assertThat(peers.size()).isGreaterThan(200);
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
  }
}
