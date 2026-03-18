package org.cardanofoundation.rosetta.api.network.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.api.network.service.SyncStatusService;
import org.cardanofoundation.rosetta.common.enumeration.SyncStage;
import org.openapitools.client.model.SyncStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncStatusHealthIndicator implements HealthIndicator {

    private final SyncStatusService syncStatusService;
    private final LedgerBlockService ledgerBlockService;

    @Override
    public Health health() {
        try {
            BlockIdentifierExtended latestBlock = ledgerBlockService.findLatestBlockIdentifier();
            Optional<SyncStatus> syncStatusOpt = syncStatusService.calculateSyncStatus(latestBlock);

            if (syncStatusOpt.isPresent()) {
                SyncStatus syncStatus = syncStatusOpt.get();
                if (Boolean.TRUE.equals(syncStatus.getSynced()) && SyncStage.LIVE.getValue().equals(syncStatus.getStage())) {
                    return Health.up()
                            .withDetail("stage", syncStatus.getStage())
                            .withDetail("currentIndex", syncStatus.getCurrentIndex())
                            .withDetail("targetIndex", syncStatus.getTargetIndex())
                            .build();
                } else {
                    return Health.down()
                            .withDetail("stage", syncStatus.getStage())
                            .withDetail("currentIndex", syncStatus.getCurrentIndex())
                            .withDetail("targetIndex", syncStatus.getTargetIndex())
                            .withDetail("message", "Node is not fully synced yet")
                            .build();
                }
            } else {
                return Health.down()
                        .withDetail("message", "Cannot determine sync status")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error calculating sync health status", e);
            return Health.down(e)
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}
