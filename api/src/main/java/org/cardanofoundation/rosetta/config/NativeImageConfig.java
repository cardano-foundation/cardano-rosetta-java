package org.cardanofoundation.rosetta.config;

import javax.annotation.Nullable;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * EXPERIMENT (exp/graalvm-native): classpath resources the api reads, for a GraalVM native image (the "native" Maven
 * profile and Dockerfile.native). A native image only contains registered resources; Spring Boot registers the
 * application*.yaml files, the tracing agent didn't record the rest. No effect on the JVM.
 */
@Configuration
@ImportRuntimeHints(NativeImageConfig.Hints.class)
public class NativeImageConfig {

  static class Hints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
      // spring.config.import=classpath:config/db-indexes.yaml (config/application.yaml)
      hints.resources().registerPattern("config/*.yaml");
      // the Rosetta API specification, read at runtime (classpath:/rosetta-specifications-1.4.15/api.yaml)
      hints.resources().registerPattern("rosetta-specifications-1.4.15/**");
      hints.resources().registerPattern("META-INF/build-info.properties");
      hints.resources().registerPattern("git.properties");
    }
  }
}
