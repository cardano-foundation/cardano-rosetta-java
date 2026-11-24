package org.cardanofoundation.rosetta;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The api uses yaci-store only as a library: a few JPA entities (registered in YaciStoreEntitiesConfig) and the
 * assets-ext repositories. No yaci-store indexer, sync job, processor or other bean may run in the api. This guards
 * that the only yaci-store beans in the context are the assets-ext repositories.
 * <p>
 * yaci-store-core (which holds the sync engine) is on the classpath for its {@code error} entity. Nothing in it runs:
 * the jar has no auto-configuration and none of its packages is component-scanned, which this test checks.
 */
class YaciStoreLibraryIsolationIntTest extends IntegrationTest {

  private static final String YACI_STORE_PACKAGE = "com.bloxbean.cardano.yaci.store.";
  private static final String ASSETS_EXT = YACI_STORE_PACKAGE + "extensions.assetstore.";

  @Autowired
  private ApplicationContext context;

  @Test
  void onlyTheAssetsExtRepositoriesAreYaciStoreBeans() {
    Set<String> yaciStoreBeanTypes = Arrays.stream(context.getBeanDefinitionNames())
        .map(context::getType)
        .filter(Objects::nonNull)
        .map(Class::getName)
        .filter(name -> name.startsWith(YACI_STORE_PACKAGE))
        .collect(Collectors.toSet());

    assertThat(yaciStoreBeanTypes).containsExactlyInAnyOrder(
        ASSETS_EXT + "cip26.storage.impl.repository.Cip26MetadataRepository",
        ASSETS_EXT + "cip26.storage.impl.repository.Cip26SyncStateRepository",
        ASSETS_EXT + "cip68.storage.impl.repository.Cip68MetadataRepository");
  }
}
