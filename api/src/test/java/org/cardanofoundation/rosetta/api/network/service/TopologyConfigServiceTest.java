package org.cardanofoundation.rosetta.api.network.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.openapitools.client.model.Peer;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TopologyConfigServiceTest extends IntegrationTest {

  @Autowired
  private TopologyConfigService topologyConfigService;

  @Test
  public void getPeersTest() {
    // when
    List<Peer> peers = topologyConfigService.getPeers();

    // then
    assertNotNull(peers);
    assertEquals(1, peers.size());
    assertEquals("preview-node.play.dev.cardano.org", peers.get(0).getPeerId());
    assertNull(peers.get(0).getMetadata());
  }

}
