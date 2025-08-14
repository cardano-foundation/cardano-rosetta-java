package org.cardanofoundation.rosetta.yaciindexer.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.yaciindexer.service.PeerDiscoveryManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PeerDiscoveryJob {

    private final PeerDiscoveryManager peerDiscoveryManager;

    @Scheduled(fixedDelay = 60000) // Run every minute
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