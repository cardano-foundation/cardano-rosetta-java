package org.cardanofoundation.rosetta.yaciindexer.services;

import com.bloxbean.cardano.yaci.core.model.BlockHeader;
import com.bloxbean.cardano.yaci.store.events.BlockHeaderEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.janino.Java;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class DBIndexService {

    private final DataSource dataSource;

    private AtomicBoolean indexRemoved =new AtomicBoolean(false);
    private AtomicBoolean indexesCreated = new AtomicBoolean(false);
    private AtomicBoolean indexesInCreation = new AtomicBoolean(false);

    @EventListener
    @Transactional
    public void handleFirstBlockEvent(BlockHeaderEvent blockHeaderEvent) {
        if (indexRemoved.get() || blockHeaderEvent.getMetadata().getBlock() > 1) {
            log.debug("Index already removed. Skipping index removal");
            return;
        }
        try {
            String scriptPath = "sql/drop-index.sql";

            log.info("Deleting optional indexes to speed-up the sync process ..... " + scriptPath);
            indexRemoved.set(true);
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScripts(
                    new ClassPathResource(scriptPath));
            populator.execute(this.dataSource);

            log.info("Optional indexes have been removed successfully.");
        } catch (Exception e) {
            log.error("Index deletion failed.", e);
        }
    }

    @EventListener
    @Transactional
    public void handleBlockEvent(BlockHeaderEvent blockHeaderEvent) {
        if(!indexesCreated.get() && !indexesInCreation.get() && blockHeaderEvent.getMetadata().isSyncMode())
            try {
                indexesInCreation.set(true);
                String scriptPath = "sql/create-index.sql";
                log.info("Re-applying optional indexes after sync process ..... " + scriptPath);

                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScripts(
                        new ClassPathResource(scriptPath));
                populator.execute(this.dataSource);
                indexesCreated.set(true);
                log.info("Optional indexes have been re-applied successfully.");
            } catch (Exception e) {
                log.error("Filed to re-apply indexes.", e);
            } finally {
                indexesInCreation.set(false);
            }
    }

}
