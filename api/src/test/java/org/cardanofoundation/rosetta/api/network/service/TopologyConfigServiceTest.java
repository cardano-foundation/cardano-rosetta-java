package org.cardanofoundation.rosetta.api.network.service;

import java.io.FileNotFoundException;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;
import org.assertj.core.api.Assertions;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openapitools.client.model.Peer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.util.FileUtils;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class TopologyConfigServiceTest {

  private TopologyConfigServiceImpl topologyConfigService = new TopologyConfigServiceImpl();

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(topologyConfigService, "topologyFilepath",
        "../config/devkit/topology.json");
  }

  @Test
  void getPeersTest_getRoots() {
    // when
    topologyConfigService.init();
    List<Peer> peers = topologyConfigService.getPeers();

    // then
    assertNotNull(peers);
    assertEquals(1, peers.size());
    assertEquals("preview-node.play.dev.cardano.org", peers.get(0).getPeerId());
    assertNull(peers.get(0).getMetadata());
  }

  @Test
  void getPeersNegativeTest_fileNotFoundException() {
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
  void getPeersNegativeTest_emptyFile() {
    // given
    try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class)) {
      fileUtils.when(() -> FileUtils.fileReader(any()))
          .thenReturn("{}");

      // when
      topologyConfigService.init();
      List<Peer> peers = topologyConfigService.getPeers();

      // then
      assertNotNull(peers);
      assertEquals(0, peers.size());
    }
  }

  @Test
  void getPeersNegativeTest_getProducers() {
    // given
    String jsonPath = this.getClass().getClassLoader()
        .getResource("testdata/topology_with_producers.json").getFile();
    ReflectionTestUtils.setField(topologyConfigService, "topologyFilepath", jsonPath);

    // when
    topologyConfigService.init();
    List<Peer> peers = topologyConfigService.getPeers();

    // then
    assertNotNull(peers);
    Assertions.assertThat(peers)
        .map(Peer::getPeerId)
        .containsExactlyInAnyOrder("addr1", "addr2");
  }

}
