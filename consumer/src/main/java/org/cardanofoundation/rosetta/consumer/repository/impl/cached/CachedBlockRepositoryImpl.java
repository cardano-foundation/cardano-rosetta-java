package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedBlockRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedBlockRepositoryImpl implements CachedBlockRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  BlockRepository blockRepository;


  @Override
  public Optional<Block> findBlockByHash(String hash) {
    return Optional.ofNullable(inMemoryCachedEntities.getBlockMap().get(hash))
        .or(() -> blockRepository.findBlockByHash(hash));
  }

  @Override
  public Boolean existsBlockByHash(byte[] hash) {
    if (inMemoryCachedEntities.getBlockMap().containsKey(hash)) {
      return true;
    }

    return blockRepository.existsBlockByHash(hash);
  }

  @Override
  public Optional<Block> findFirstByEpochNo(Integer epochNo) {
    return blockRepository.findFirstByEpochNo(epochNo)
        .or(() -> inMemoryCachedEntities.getBlockMap().values().stream()
            .filter(block -> Objects.equals(block.getEpochNo(), epochNo))
            .min(Comparator.comparing(Block::getBlockNo)));
  }

  @Override
  public Block save(Block entity) {
    inMemoryCachedEntities.getBlockMap().put(entity.getHash(), entity);
    return entity;
  }

  @Override
  public List<Block> saveAll(Collection<Block> entities) {
    entities.forEach(block ->
        inMemoryCachedEntities.getBlockMap().put(block.getHash(), block));
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    long startTime = System.currentTimeMillis();
    var blocks = inMemoryCachedEntities.getBlockMap().values();
    var blocksSize = blocks.size();
    blockRepository.saveAll(blocks);
    inMemoryCachedEntities.getBlockMap().clear();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("Block {} elapsed: {} ms, {} second(s)", blocksSize, totalTime, totalTime / 1000f);
  }
}
