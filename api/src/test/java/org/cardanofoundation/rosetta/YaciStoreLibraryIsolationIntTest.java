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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The api uses yaci-store (assets-ext) only as a library: its JPA entities and repositories.
 * No yaci-store indexer, sync job, processor or other bean may run in the api. This guards that:
 * <ul>
 *   <li>the only yaci-store beans in the context are the assets-ext repositories, and</li>
 *   <li>the yaci-store sync engine (yaci-store-core) is not even on the classpath.</li>
 * </ul>
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

  @Test
  void yaciStoreSyncEngineIsNotOnTheClasspath() {
    assertThatThrownBy(() -> Class.forName(YACI_STORE_PACKAGE + "core.service.StartService"))
        .isInstanceOf(ClassNotFoundException.class);
  }
}
