package org.cardanofoundation.rosetta.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypesScanner;

import com.bloxbean.cardano.yaci.store.core.storage.impl.model.ErrorEntity;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.impl.model.Cip26Metadata;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip26.storage.impl.model.Cip26SyncState;
import com.bloxbean.cardano.yaci.store.extensions.assetstore.cip68.storage.impl.model.Cip68Metadata;
import com.bloxbean.cardano.yaci.store.governance.storage.impl.model.DelegationVoteEntity;

/**
 * JPA entities of the api: Rosetta's own (from {@code @EntityScan}) plus the yaci-store entities it reads,
 * used as a library instead of copies. The tables are written by yaci-indexer.
 * <p>
 * The yaci-store entities are listed one by one, not scanned by package: their packages also hold entities
 * the api doesn't use, and some share a class name with Rosetta's own (yaci-store's governance
 * {@code VotingProcedureEntity}), which would clash as JPA entity names.
 */
@Configuration
public class YaciStoreEntitiesConfig {

  static final List<Class<?>> YACI_STORE_ENTITIES = List.of(
      Cip26Metadata.class,
      Cip26SyncState.class,
      Cip68Metadata.class,
      DelegationVoteEntity.class,
      ErrorEntity.class);

  @Bean
  public PersistenceManagedTypes persistenceManagedTypes(BeanFactory beanFactory, ResourceLoader resourceLoader) {
    String[] rosettaPackages = EntityScanPackages.get(beanFactory).getPackageNames().toArray(String[]::new);
    PersistenceManagedTypes scanned = new PersistenceManagedTypesScanner(resourceLoader).scan(rosettaPackages);

    List<String> managedClassNames = new ArrayList<>(scanned.getManagedClassNames());
    YACI_STORE_ENTITIES.forEach(entity -> managedClassNames.add(entity.getName()));

    return PersistenceManagedTypes.of(managedClassNames, scanned.getManagedPackages());
  }
}
