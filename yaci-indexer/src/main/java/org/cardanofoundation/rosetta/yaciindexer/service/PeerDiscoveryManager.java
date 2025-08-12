package org.cardanofoundation.rosetta.yaciindexer.service;

import com.bloxbean.cardano.yaci.core.protocol.peersharing.messages.PeerAddress;
import com.bloxbean.cardano.yaci.helper.PeerDiscovery;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class PeerDiscoveryManager {

    public static final int PEERS_REQUEST_AMOUNT = 50;
    public static final int PEERS_REQUEST_TIMEOUT_SECS = 60;

    @Value("${store.cardano.host:preprod-node.world.dev.cardano.org}")
    private String cardanoNodeHost;

    @Value("${store.cardano.port:30000}")
    private int cardanoNodePort;

    @Value("${store.cardano.protocol-magic:1}")
    private long protocolMagic;

    @Getter
    private final CopyOnWriteArrayList<PeerAddress> cachedPeers = new CopyOnWriteArrayList<>();

    public void updateCachedPeers(List<PeerAddress> peers) {
        this.cachedPeers.clear();
        this.cachedPeers.addAll(peers);

        log.debug("Updated cached peers: {} peers available", this.cachedPeers.size());
    }

    public List<PeerAddress> discoverPeers() {
        log.info("Discovering cardano-node peers...");

        PeerDiscovery peerDiscovery = new PeerDiscovery(
                cardanoNodeHost,
                cardanoNodePort,
                protocolMagic,
                PEERS_REQUEST_AMOUNT
        );

        try {
            Mono<List<PeerAddress>> peersMono = peerDiscovery.discover();

            return peersMono.block(Duration.ofSeconds(PEERS_REQUEST_TIMEOUT_SECS));
        } finally {
            peerDiscovery.shutdown();
        }
    }

}
