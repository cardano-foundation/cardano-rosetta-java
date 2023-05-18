package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.TxMetadata;
import org.cardanofoundation.rosetta.consumer.repository.TxMetadataRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxMetadataRepository;
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
public class CachedTxMetadataRepositoryImpl implements CachedTxMetadataRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  TxMetadataRepository txMetadataRepository;

  @Override
  public List<TxMetadata> saveAll(Collection<TxMetadata> entities) {
    inMemoryCachedEntities.getTxMetadata().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    txMetadataRepository.saveAll(inMemoryCachedEntities.getTxMetadata());
    inMemoryCachedEntities.getTxMetadata().clear();
  }
}
