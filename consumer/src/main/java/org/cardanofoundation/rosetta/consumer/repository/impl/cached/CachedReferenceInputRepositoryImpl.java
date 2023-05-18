package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.ReferenceTxIn;
import org.cardanofoundation.rosetta.consumer.repository.ReferenceInputRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedReferenceInputRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedReferenceInputRepositoryImpl implements CachedReferenceInputRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  ReferenceInputRepository referenceInputRepository;

  @Override
  public List<ReferenceTxIn> saveAll(Collection<ReferenceTxIn> entities) {
    inMemoryCachedEntities.getReferenceTxIns().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    referenceInputRepository.saveAll(inMemoryCachedEntities.getReferenceTxIns());
    inMemoryCachedEntities.getReferenceTxIns().clear();
  }
}
