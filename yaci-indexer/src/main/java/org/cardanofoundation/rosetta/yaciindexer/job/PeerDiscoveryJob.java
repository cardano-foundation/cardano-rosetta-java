package org.cardanofoundation.rosetta.yaciindexer.job;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.yaciindexer.service.PeerDiscoveryManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.MINUTES;

@Service
@Slf4j
@RequiredArgsConstructor
// peer discovery disabled by default
@ConditionalOnProperty(name = "jobs.peer-discovery.enabled", havingValue = "true")
public class PeerDiscoveryJob {

    private final PeerDiscoveryManager peerDiscoveryManager;

    @PostConstruct
    public void init() {
        log.info("Peer discovery activated.");
    }

    // Initial refresh after 1 minute, then every hour
    @Scheduled(initialDelay = 5, fixedDelay = 60, timeUnit = MINUTES)
    public void refreshPeers() {
        log.info("Starting a periodic / scheduled peer discovery refresh...");

        try {
            Optional.ofNullable(peerDiscoveryManager.discoverPeers())
                    .filter(l -> !l.isEmpty())
                    .ifPresent(peers -> {
                        peerDiscoveryManager.updateCachedPeers(peers);
                        log.info("Successfully refreshed peer cache with {} peers", peers.size());
                    });

        } catch (Exception e) {
            log.error("Failed to refresh peer cache during scheduled discovery", e);
            // Don't update cache on error to preserve last known good state
        }
    }

}