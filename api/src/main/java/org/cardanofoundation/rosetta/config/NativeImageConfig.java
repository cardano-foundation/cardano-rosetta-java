package org.cardanofoundation.rosetta.config;

import javax.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.jooq.DataType;
import org.jooq.impl.SQLDataType;
import org.jooq.util.postgres.PostgresDataType;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
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

    private static final List<String> HYPERSISTENCE_JSON_TYPES = List.of(
      "io.hypersistence.utils.hibernate.type.HibernateTypesContributor",
      "io.hypersistence.utils.hibernate.type.json.internal.AbstractJsonJdbcTypeDescriptor",
      "io.hypersistence.utils.hibernate.type.json.internal.JacksonUtil",
      "io.hypersistence.utils.hibernate.type.json.internal.JdbcTypeSetter",
      "io.hypersistence.utils.hibernate.type.json.internal.JsonBinaryJdbcTypeDescriptor",
      "io.hypersistence.utils.hibernate.type.json.internal.JsonBlobJdbcTypeDescriptor",
      "io.hypersistence.utils.hibernate.type.json.internal.JsonBytesJdbcTypeDescriptor",
      "io.hypersistence.utils.hibernate.type.json.internal.JsonClobJdbcTypeDescriptor",
      "io.hypersistence.utils.hibernate.type.json.internal.JsonJavaTypeDescriptor",
      "io.hypersistence.utils.hibernate.type.json.internal.JsonJdbcTypeDescriptor",
      "io.hypersistence.utils.hibernate.type.json.internal.JsonNodeJavaTypeDescriptor",
      "io.hypersistence.utils.hibernate.type.json.internal.JsonStringJdbcTypeDescriptor",
      "io.hypersistence.utils.hibernate.type.json.JsonBinaryType",
      "io.hypersistence.utils.hibernate.type.json.JsonBlobType",
      "io.hypersistence.utils.hibernate.type.json.JsonClobType",
      "io.hypersistence.utils.hibernate.type.json.JsonNodeBinaryType",
      "io.hypersistence.utils.hibernate.type.json.JsonNodeStringType",
      "io.hypersistence.utils.hibernate.type.json.JsonStringType",
      "io.hypersistence.utils.hibernate.type.json.JsonType",
      "io.hypersistence.utils.hibernate.type.MutableDynamicParameterizedType",
      "io.hypersistence.utils.hibernate.type.MutableType",
      "io.hypersistence.utils.hibernate.type.util.ClassImportIntegrator",
      "io.hypersistence.utils.hibernate.type.util.Configuration",
      "io.hypersistence.utils.hibernate.type.util.JsonConfiguration",
      "io.hypersistence.utils.hibernate.type.util.JsonSerializer",
      "io.hypersistence.utils.hibernate.type.util.JsonSerializerSupplier",
      "io.hypersistence.utils.hibernate.type.util.ObjectMapperJsonSerializer",
      "io.hypersistence.utils.hibernate.type.util.ObjectMapperSupplier",
      "io.hypersistence.utils.hibernate.type.util.ObjectMapperWrapper",
      "io.hypersistence.utils.hibernate.type.util.ParameterizedParameterType",
      "io.hypersistence.utils.hibernate.type.util.ParameterTypeUtils");

    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
      // spring.config.import=classpath:config/db-indexes.yaml (config/application.yaml)
      hints.resources().registerPattern("config/*.yaml");
      // the Rosetta API specification, read at runtime (classpath:/rosetta-specifications-1.4.15/api.yaml)
      hints.resources().registerPattern("rosetta-specifications-1.4.15/**");
      hints.resources().registerPattern("META-INF/build-info.properties");
      hints.resources().registerPattern("git.properties");
      // Hibernate instantiates @Type(JsonType.class) reflectively; the tracing agent recorded JsonType without its
      // constructors. Register the Hypersistence JSON type classes and the type descriptors they use.
      HYPERSISTENCE_JSON_TYPES.forEach(type -> hints.reflection().registerType(TypeReference.of(type),
          MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS,
          MemberCategory.DECLARED_FIELDS));
      // jOOQ's static initialization (SQLDataType, PostgresDataType) creates the array data type of every built-in
      // type, e.g. Decfloat[]. In a native image an array class the analysis didn't see isn't in the image and
      // Class.arrayType() returns null, which fails DefaultDataType.<clinit> with a NullPointerException.
      registerJooqArrayTypes(hints, SQLDataType.class);
      registerJooqArrayTypes(hints, PostgresDataType.class);
    }

    private static void registerJooqArrayTypes(RuntimeHints hints, Class<?> dataTypes) {
      for (Field field : dataTypes.getFields()) {
        if (!Modifier.isStatic(field.getModifiers()) || !DataType.class.isAssignableFrom(field.getType())) {
          continue;
        }
        try {
          Class<?> type = ((DataType<?>) field.get(null)).getType();
          hints.reflection().registerType(type.arrayType());
        } catch (IllegalAccessException e) {
          throw new IllegalStateException("Cannot read jOOQ data type " + field, e);
        }
      }
    }
  }
}
