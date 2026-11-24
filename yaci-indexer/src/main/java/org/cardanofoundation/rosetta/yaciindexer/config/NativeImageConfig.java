package org.cardanofoundation.rosetta.yaciindexer.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nullable;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;
import org.jooq.DSLContext;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.NativeDetector;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.bloxbean.cardano.yaci.store.common.config.StoreProperties;
import com.bloxbean.cardano.yaci.store.core.storage.impl.CursorRepository;
import com.bloxbean.cardano.yaci.store.core.storage.impl.CursorStorageImpl;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.AddressStorageImpl;
import com.bloxbean.cardano.yaci.store.blocks.storage.impl.BlockCborStorageImpl;
import com.bloxbean.cardano.yaci.store.blocks.storage.impl.BlockStorageImpl;
import com.bloxbean.cardano.yaci.store.blocks.storage.impl.mapper.BlockMapper;
import com.bloxbean.cardano.yaci.store.blocks.storage.impl.repository.BlockCborRepository;
import com.bloxbean.cardano.yaci.store.blocks.storage.impl.repository.BlockRepository;
import com.bloxbean.cardano.yaci.store.epoch.storage.impl.EpochParamStorageImpl;
import com.bloxbean.cardano.yaci.store.epoch.storage.impl.mapper.ProtocolParamsMapper;
import com.bloxbean.cardano.yaci.store.epoch.storage.impl.repository.CostModelRepository;
import com.bloxbean.cardano.yaci.store.epoch.storage.impl.repository.EpochParamRepository;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.TransactionCborStorageImpl;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.TransactionStorageImpl;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.TransactionWitnessStorageImpl;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.mapper.TxnMapper;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.repository.TxnCborRepository;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.repository.TxnEntityRepository;
import com.bloxbean.cardano.yaci.store.transaction.storage.impl.repository.TxnWitnessRepository;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.UtxoCache;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.UtxoStorageImpl;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.repository.TxInputRepository;
import com.bloxbean.cardano.yaci.store.utxo.storage.impl.repository.UtxoRepository;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * EXPERIMENT (exp/graalvm-native): support for running yaci-indexer as a GraalVM native image (the "native" Maven
 * profile and Dockerfile.native). Based on cf-adahandle-resolver's NativeImageConfig, which runs yaci-store natively.
 * The Flyway provider and runtime hints only apply in a native image; the storage beans are the same beans yaci-store
 * would create, declared by their implementation type.
 */
@Configuration
@ImportRuntimeHints(NativeImageConfig.Hints.class)
public class NativeImageConfig {

  /**
   * Spring Boot's native Flyway resource provider lists a classpath location in the first jar that contains it only.
   * yaci-store ships migrations for the same location (db/store/postgresql) in several jars (core, utxo, blocks, ...)
   * and Rosetta adds its own, so the others would be silently skipped. Boot only installs its provider when none is
   * set, so this one takes precedence.
   */
  @Bean
  public FlywayConfigurationCustomizer allJarsMigrationResourceProvider() {
    return configuration -> {
      if (NativeDetector.inNativeImage()) {
        configuration.resourceProvider(new AllJarsResourceProvider(configuration.getClassLoader(),
            Arrays.asList(configuration.getLocations()), configuration.getEncoding()));
      }
    };
  }

  /*
   * yaci-store declares these @Transactional beans by their interface type (CursorStorage, AddressStorage), so AOT
   * can't pregenerate the CGLIB proxy of the implementation class and a native image can't generate it at runtime.
   * Declaring the implementation type here (yaci-store's definitions are @ConditionalOnMissingBean) lets AOT generate
   * the proxies. Same beans on the JVM.
   */
  @Bean
  public CursorStorageImpl cursorStorage(CursorRepository cursorRepository) {
    return new CursorStorageImpl(cursorRepository);
  }

  @Bean
  public AddressStorageImpl addressStorage(DSLContext dslContext, StoreProperties storeProperties) {
    return new AddressStorageImpl(dslContext, storeProperties);
  }

  // The other @Transactional storage beans of the yaci-store modules Rosetta's indexer uses (blocks, transaction,
  // epoch, utxo), found by scanning yaci-store's configurations for interface-typed beans with a @Transactional
  // implementation. Same constructor arguments as yaci-store's definitions.

  @Bean
  public BlockStorageImpl blockStorage(BlockRepository blockRepository, BlockMapper blockMapper) {
    return new BlockStorageImpl(blockRepository, blockMapper);
  }

  @Bean
  public BlockCborStorageImpl blockCborStorage(BlockCborRepository blockCborRepository) {
    return new BlockCborStorageImpl(blockCborRepository);
  }

  @Bean
  public EpochParamStorageImpl epochParamStorage(EpochParamRepository epochParamRepository,
      CostModelRepository costModelRepository, ProtocolParamsMapper protocolParamsMapper) {
    return new EpochParamStorageImpl(epochParamRepository, costModelRepository, protocolParamsMapper);
  }

  @Bean
  public TransactionStorageImpl transactionStorage(TxnEntityRepository txnEntityRepository, TxnMapper txnMapper,
      DSLContext dslContext) {
    return new TransactionStorageImpl(txnEntityRepository, txnMapper, dslContext);
  }

  @Bean
  public TransactionCborStorageImpl transactionCborStorage(TxnCborRepository txnCborRepository) {
    return new TransactionCborStorageImpl(txnCborRepository);
  }

  @Bean
  public TransactionWitnessStorageImpl transactionWitnessStorage(TxnWitnessRepository txnWitnessRepository,
      TxnMapper txnMapper, DSLContext dslContext) {
    return new TransactionWitnessStorageImpl(txnWitnessRepository, txnMapper, dslContext);
  }

  @Bean
  public UtxoStorageImpl utxoStorage(UtxoRepository utxoRepository, TxInputRepository spentOutputRepository,
      DSLContext dslContext, UtxoCache utxoCache, PlatformTransactionManager transactionManager) {
    return new UtxoStorageImpl(utxoRepository, spentOutputRepository, dslContext, utxoCache, transactionManager);
  }

  static class AllJarsResourceProvider implements ResourceProvider {

    private final ClassLoader classLoader;
    private final Collection<Location> locations;
    private final Charset encoding;
    @Nullable
    private Map<String, LoadableResource> resources;

    AllJarsResourceProvider(ClassLoader classLoader, Collection<Location> locations, Charset encoding) {
      this.classLoader = classLoader;
      this.locations = locations;
      this.encoding = encoding;
    }

    /** Null when there is no such resource, as Flyway's {@code ResourceProvider} contract requires. */
    @Override
    @Nullable
    public LoadableResource getResource(String name) {
      if (classLoader.getResource(name) == null) {
        return null;
      }
      return new ClassPathResource(null, name, classLoader, encoding);
    }

    @Override
    public synchronized Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
      if (resources == null) {
        resources = findAll();
      }
      List<LoadableResource> result = new ArrayList<>();
      for (LoadableResource resource : resources.values()) {
        String filename = resource.getFilename();
        if (filename.startsWith(prefix) && Arrays.stream(suffixes).anyMatch(filename::endsWith)) {
          result.add(resource);
        }
      }
      return result;
    }

    private Map<String, LoadableResource> findAll() {
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
      Map<String, LoadableResource> found = new LinkedHashMap<>();
      for (Location location : locations) {
        if (!location.isClassPath()) {
          continue;
        }
        try {
          // classpath*: visits the location in every jar, not only the first one.
          for (Resource resource : resolver.getResources("classpath*:" + location.getPath() + "/*")) {
            String filename = resource.getFilename();
            if (filename == null || filename.isEmpty()) {
              continue;
            }
            String path = location.getPath() + "/" + filename;
            found.putIfAbsent(path, new ClassPathResource(location, path, classLoader, encoding));
          }
        } catch (IOException e) {
          throw new UncheckedIOException("Failed to list migrations in " + location.getDescriptor(), e);
        }
      }
      return found;
    }
  }

  static class Hints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
      // Netty (yaci's node client) reads java.nio.Bits.UNALIGNED through Unsafe while initializing; on the JVM this
      // bypasses reflection, so neither AOT nor the tracing agent records it.
      hints.reflection().registerType(TypeReference.of("java.nio.Bits"), type -> type.withField("UNALIGNED"));
      // spring.config.import=classpath:config/db-indexes.yaml (application.properties)
      hints.resources().registerPattern("config/*.yaml");
      // Flyway migrations from all jars and vendors: yaci-store's and Rosetta's (db/store/{vendor}).
      hints.resources().registerPattern("db/store/**");
      hints.resources().registerPattern("META-INF/build-info.properties");
      hints.resources().registerPattern("git.properties");
    }
  }
}
