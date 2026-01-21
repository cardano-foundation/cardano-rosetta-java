package org.cardanofoundation.rosetta.api.network.service.postgresql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.network.service.IndexCreationMonitor;
import org.jooq.DSLContext;
import org.jooq.Record5;
import org.jooq.Result;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * PostgreSQL-specific implementation of IndexCreationMonitor.
 * Queries the pg_stat_progress_create_index system view to determine
 * if any indexes are currently being created.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"!h2 & !test-integration"})
public class PostgreSQLIndexCreationMonitor implements IndexCreationMonitor {

    private final DSLContext dslContext;

    @Override
    public boolean isCreatingIndexes() {
        try {
            Result<Record5<String, Long, Long, Long, Long>> result = dslContext
                .select(
                    org.jooq.impl.DSL.field("phase", String.class),
                    org.jooq.impl.DSL.field("blocks_total", Long.class),
                    org.jooq.impl.DSL.field("blocks_done", Long.class),
                    org.jooq.impl.DSL.field("tuples_total", Long.class),
                    org.jooq.impl.DSL.field("tuples_done", Long.class)
                )
                .from("pg_stat_progress_create_index")
                .fetch();

            boolean creating = !result.isEmpty();

            if (creating) {
                log.info("[IndexMonitor] Found {} index(es) currently being created", result.size());
                result.forEach(record -> log.debug(
                    "[IndexMonitor] Index creation in progress - phase: {}, blocks: {}/{}, tuples: {}/{}",
                    record.value1(),
                    record.value3(),
                    record.value2(),
                    record.value5(),
                    record.value4()
                ));
            } else {
                log.debug("[IndexMonitor] No indexes currently being created");
            }

            return creating;
        } catch (Exception e) {
            log.error("[IndexMonitor] Error checking index creation status", e);
            // In case of error, assume no indexes are being created to avoid blocking sync
            return false;
        }
    }

    @Override
    public List<IndexCreationProgress> getIndexCreationProgress() {
        try {
            Result<Record5<String, Long, Long, Long, Long>> result = dslContext
                .select(
                    org.jooq.impl.DSL.field("phase", String.class),
                    org.jooq.impl.DSL.field("blocks_total", Long.class),
                    org.jooq.impl.DSL.field("blocks_done", Long.class),
                    org.jooq.impl.DSL.field("tuples_total", Long.class),
                    org.jooq.impl.DSL.field("tuples_done", Long.class)
                )
                .from("pg_stat_progress_create_index")
                .fetch();

            return result.stream()
                .map(record -> new IndexCreationProgress(
                    record.value1(),
                    record.value2(),
                    record.value3(),
                    record.value4(),
                    record.value5()
                ))
                .toList();
        } catch (Exception e) {
            log.error("[IndexMonitor] Error fetching index creation progress", e);
            return Collections.emptyList();
        }
    }
}
