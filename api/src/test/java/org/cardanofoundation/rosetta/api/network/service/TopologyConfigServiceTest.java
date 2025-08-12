package org.cardanofoundation.rosetta.api.network.service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.springframework.test.util.ReflectionTestUtils;
import org.assertj.core.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.Peer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.client.YaciHttpGateway;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.util.FileUtils;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TopologyConfigServiceTest {

  @Mock
  private YaciHttpGateway yaciHttpGateway;

  private TopologyConfigServiceImpl topologyConfigService;

  @BeforeEach
  void setup() {
    topologyConfigService = new TopologyConfigServiceImpl(yaciHttpGateway);
    ReflectionTestUtils.setField(topologyConfigService, "topologyFilepath",
        "../config/node/devkit/topology.json");
  }

  @Test
  void getPeersTest_getStaticRoots() {
    // when
    topologyConfigService.init();
    List<Peer> peers = topologyConfigService.getStaticPeers();

    // then
    assertNotNull(peers);
    assertEquals(1, peers.size());
    var aPeer = peers.getFirst();

    assertEquals("preview-node.play.dev.cardano.org:3001", aPeer.getPeerId());

    Map metadata = (Map) aPeer.getMetadata();

    assertEquals("IPv4", metadata.get("type"));
  }

  @Test
  void getStaticPeersNegativeTest_fileNotFoundException() {
    // given
    try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class)) {
      fileUtils.when(() -> FileUtils.fileReader(any()))
          .thenThrow(new FileNotFoundException());

      // when
      ApiException exception = assertThrows(ApiException.class,
          () -> topologyConfigService.init());

      // then
      assertNotNull(exception);
      assertEquals(RosettaErrorType.CONFIG_NOT_FOUND.getMessage(),
          exception.getError().getMessage());
    }
  }

  @Test
  void getStaticPeersNegativeTest_emptyFile() {
    // given
    try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class)) {
      fileUtils.when(() -> FileUtils.fileReader(any()))
          .thenReturn("{}");

      // when
      topologyConfigService.init();
      List<Peer> peers = topologyConfigService.getStaticPeers();

      // then
      assertNotNull(peers);
      assertEquals(0, peers.size());
    }
  }

  @Test
  void getPeersNegativeTest_getStaticProducers() {
    // given
    String jsonPath = this.getClass().getClassLoader()
        .getResource("testdata/topology_with_producers.json").getFile();
    ReflectionTestUtils.setField(topologyConfigService, "topologyFilepath", jsonPath);

    // when
    topologyConfigService.init();
    List<Peer> peers = topologyConfigService.getStaticPeers();

    // then
    assertNotNull(peers);
    assertEquals(0, peers.size()); // Should be empty as no bootstrap peers
  }

  @Test
  void getStaticPeers_withBootstrapPeers() {
    // given
    String jsonPath = this.getClass().getClassLoader()
        .getResource("testdata/topology_with_bootstrap_peers.json").getFile();
    ReflectionTestUtils.setField(topologyConfigService, "topologyFilepath", jsonPath);

    // when
    topologyConfigService.init();
    List<Peer> peers = topologyConfigService.getStaticPeers();

    // then
    assertNotNull(peers);
    assertEquals(2, peers.size());
    Assertions.assertThat(peers)
        .map(Peer::getPeerId)
        .containsExactlyInAnyOrder("test1.cardano.org:3001", "test2.cardano.org:3002");
  }

}
