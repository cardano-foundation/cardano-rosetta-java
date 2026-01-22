package org.cardanofoundation.rosetta.api.network.service.postgresql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.network.service.IndexCreationMonitor;
import org.cardanofoundation.rosetta.api.network.service.RosettaIndexConfig;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Result;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PostgreSQL-specific implementation of IndexCreationMonitor.
 * Queries the pg_index system view to check if required indices are valid and ready.
 * Only when ALL required indices exist and are both valid (indisvalid=true) and
 * ready (indisready=true), the indexes are considered fully applied.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"!h2 & !test-integration"})
public class PostgreSQLIndexCreationMonitor implements IndexCreationMonitor {

    private final DSLContext dslContext;
    private final RosettaIndexConfig rosettaIndexConfig;

    @PostConstruct
    public void init() {
        List<String> requiredIndexes = rosettaIndexConfig.getDbIndexes();
        log.info("[IndexMonitor] Monitoring {} required indices: {}",
            requiredIndexes != null ? requiredIndexes.size() : 0,
            requiredIndexes);
    }

    @Override
    public boolean isCreatingIndexes() {
        List<String> requiredIndexes = rosettaIndexConfig.getDbIndexes();

        if (requiredIndexes == null || requiredIndexes.isEmpty()) {
            log.warn("[IndexMonitor] No required indexes configured, assuming all ready");
            return false;
        }

        try {
            // Query pg_index system catalog to check index validity and readiness
            // We join with pg_class to get the index name
            Result<Record3<String, Boolean, Boolean>> result = dslContext
                .select(
                    org.jooq.impl.DSL.field("c.relname", String.class),
                    org.jooq.impl.DSL.field("i.indisvalid", Boolean.class),
                    org.jooq.impl.DSL.field("i.indisready", Boolean.class)
                )
                .from("pg_index i")
                .join("pg_class c").on("i.indexrelid = c.oid")
                .where(org.jooq.impl.DSL.field("c.relname").in(requiredIndexes))
                .fetch();

            // Build a map of index name -> status
            Map<String, IndexStatus> indexStatusMap = result.stream()
                .collect(Collectors.toMap(
                    record -> record.value1(),
                    record -> new IndexStatus(record.value2(), record.value3())
                ));

            // Check if all required indexes exist and are valid + ready
            boolean allIndexesReady = true;
            List<String> missingIndexes = requiredIndexes.stream()
                .filter(indexName -> !indexStatusMap.containsKey(indexName))
                .toList();

            List<String> notReadyIndexes = requiredIndexes.stream()
                .filter(indexName -> {
                    IndexStatus status = indexStatusMap.get(indexName);
                    return status != null && (!status.isValid || !status.isReady);
                })
                .toList();

            if (!missingIndexes.isEmpty()) {
                log.info("[IndexMonitor] Missing indices ({}): {}", missingIndexes.size(), missingIndexes);
                allIndexesReady = false;
            }

            if (!notReadyIndexes.isEmpty()) {
                log.info("[IndexMonitor] Indices not ready or not valid ({}): {}",
                    notReadyIndexes.size(), notReadyIndexes);
                notReadyIndexes.forEach(indexName -> {
                    IndexStatus status = indexStatusMap.get(indexName);
                    log.debug("[IndexMonitor] Index '{}' status: valid={}, ready={}",
                        indexName, status.isValid, status.isReady);
                });
                allIndexesReady = false;
            }

            // isCreatingIndexes returns true if NOT all indexes are ready
            // (meaning we're still in APPLYING_INDEXES state)
            boolean creating = !allIndexesReady;

            if (creating) {
                log.info("[IndexMonitor] Indices are still being applied or not ready");
            } else {
                log.debug("[IndexMonitor] All required indices are valid and ready");
            }

            return creating;
        } catch (Exception e) {
            log.error("[IndexMonitor] Error checking index status", e);
            // In case of error, assume indexes are being created to stay in APPLYING_INDEXES state
            // This is safer than assuming LIVE state when we can't verify
            return true;
        }
    }

    @Override
    public List<IndexCreationProgress> getIndexCreationProgress() {
        List<String> requiredIndexes = rosettaIndexConfig.getDbIndexes();

        if (requiredIndexes == null || requiredIndexes.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // Query pg_index to get status of required indexes
            Result<Record3<String, Boolean, Boolean>> result = dslContext
                .select(
                    org.jooq.impl.DSL.field("c.relname", String.class),
                    org.jooq.impl.DSL.field("i.indisvalid", Boolean.class),
                    org.jooq.impl.DSL.field("i.indisready", Boolean.class)
                )
                .from("pg_index i")
                .join("pg_class c").on("i.indexrelid = c.oid")
                .where(org.jooq.impl.DSL.field("c.relname").in(requiredIndexes))
                .fetch();

            return result.stream()
                .map(record -> {
                    String indexName = record.value1();
                    boolean isValid = record.value2();
                    boolean isReady = record.value3();

                    // Map to IndexCreationProgress
                    // Use phase to indicate status
                    String phase = isValid && isReady ? "ready" :
                                  isReady ? "validating" : "building";

                    return new IndexCreationProgress(
                        indexName + " - " + phase,
                        null, // blocks_total not available
                        null, // blocks_done not available
                        null, // tuples_total not available
                        null  // tuples_done not available
                    );
                })
                .toList();
        } catch (Exception e) {
            log.error("[IndexMonitor] Error fetching index status", e);
            return Collections.emptyList();
        }
    }

    /**
     * Helper record to track index status
     */
    private record IndexStatus(boolean isValid, boolean isReady) {}
}
