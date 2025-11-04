package org.cardanofoundation.rosetta.yaciindexer.service;

import com.bloxbean.cardano.yaci.core.protocol.peersharing.messages.PeerAddress;
import com.bloxbean.cardano.yaci.helper.PeerDiscovery;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class PeerDiscoveryManager {

    /**
     * Maximum number of peers to request from the Cardano node and cache.
     * Limited to avoid excessive peer lists and allow randomization.
     */
    public static final int PEERS_REQUEST_AMOUNT = 25;
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
        // Shuffle to randomize peer selection and limit to PEERS_REQUEST_AMOUNT
        // This ensures we don't always return the same peers
        List<PeerAddress> shuffledPeers = new ArrayList<>(peers);
        Collections.shuffle(shuffledPeers);

        List<PeerAddress> limitedPeers = shuffledPeers.stream()
            .limit(PEERS_REQUEST_AMOUNT)
            .toList();

        this.cachedPeers.clear();
        this.cachedPeers.addAll(limitedPeers);

        log.debug("Updated cached peers: {} peers available (from {} discovered)",
            this.cachedPeers.size(), peers.size());
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
