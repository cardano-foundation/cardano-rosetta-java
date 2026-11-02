package org.cardanofoundation.rosetta.yaciindexer.config;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.hypersistence.utils.hibernate.type.util.JsonSerializer;
import io.hypersistence.utils.hibernate.type.util.ObjectMapperWrapper;
import org.hibernate.internal.util.SerializationHelper;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.TypeFactory;

/**
 * Hypersistence Utils 3.16 clones JSON values with Java serialization and rejects graphs that
 * contain a non-serializable type, such as Yaci {@code Relay}. This serializer keeps that fast
 * path and falls back to a Jackson round-trip for the rest.
 *
 * <p>Registered with {@code hypersistence.utils.json.serializer}.
 */
public class CloningJsonSerializer implements JsonSerializer {

  @Override
  @SuppressWarnings("unchecked")
  public <T> T clone(T value) {
    if (value == null || value instanceof String) {
      return value;
    }
    if (value instanceof JsonNode node) {
      return (T) node.deepCopy();
    }
    if (value instanceof Optional<?> optional) {
      return (T) optional.map(this::clone);
    }
    if (value instanceof Serializable serializable) {
      try {
        return (T) SerializationHelper.clone(serializable);
      } catch (RuntimeException ignored) {
        // A nested value is not Serializable (for example yaci Relay inside a List).
      }
    }
    return jacksonClone(value);
  }

  @SuppressWarnings("unchecked")
  private <T> T jacksonClone(T value) {
    ObjectMapper mapper = ObjectMapperWrapper.INSTANCE.getObjectMapper();
    try {
      return mapper.readValue(mapper.writeValueAsString(value), javaType(mapper, value));
    } catch (JacksonException exception) {
      throw new IllegalStateException(
          "Unable to clone JSON attribute of type " + value.getClass().getName(), exception);
    }
  }

  private static JavaType javaType(ObjectMapper mapper, Object value) {
    TypeFactory types = mapper.getTypeFactory();
    if (value instanceof Collection<?> collection) {
      Class<?> contentType = collection.stream()
          .filter(Objects::nonNull)
          .map(Object::getClass)
          .findFirst()
          .orElse(Object.class);
      Class<? extends Collection> collectionType = value instanceof Set<?> ? Set.class : List.class;
      return types.constructCollectionType(collectionType, contentType);
    }
    return types.constructType(value.getClass());
  }
}
