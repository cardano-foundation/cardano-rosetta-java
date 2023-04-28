package org.cardanofoundation.rosetta.consumer.repository.cached;

import com.sotatek.cardano.common.entity.BaseEntity;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * This repository implements calls to both in-memory entities
 * and JPA repositories as fallback
 */
public interface BaseCachedRepository<T extends BaseEntity> {

  default T save(T entity) {
    return throwISE();
  }

  default List<T> saveAll(Collection<T> entities) {
    return throwISE();
  }

  void flushToDb();

  default <E extends BaseEntity> boolean isNew(E entity) {
    return Objects.isNull(entity.getId());
  }

  static <E> E throwISE() {
    throw new IllegalStateException("Operation not supported");
  }
}
