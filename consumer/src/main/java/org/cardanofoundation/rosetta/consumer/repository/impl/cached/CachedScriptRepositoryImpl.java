package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.consumer.projection.ScriptProjection;
import org.cardanofoundation.rosetta.consumer.repository.ScriptRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedScriptRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedScriptRepositoryImpl implements CachedScriptRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  ScriptRepository scriptRepository;

  @Override
  public Map<String, Script> getScriptByHashes(Set<String> hashes) {
    Set<String> nonExistentScriptHash = new HashSet<>();
    Map<String, Script> scriptMap = new ConcurrentHashMap<>();

    hashes.forEach(scriptHash -> {
      Script script = inMemoryCachedEntities.getScriptMap().get(scriptHash);
      if (Objects.isNull(script)) {
        nonExistentScriptHash.add(scriptHash);
        return;
      }

      scriptMap.put(scriptHash, script);
    });

    if (!CollectionUtils.isEmpty(nonExistentScriptHash)) {
      scriptMap.putAll(scriptRepository.getScriptByHashes(nonExistentScriptHash)
          .stream()
          .collect(Collectors.toMap(
              ScriptProjection::getHash,
              scriptProjection -> Script.builder()
                  .id(scriptProjection.getId())
                  .hash(scriptProjection.getHash())
                  .build())
          )
      );
    }

    return scriptMap;
  }

  @Override
  public List<Script> saveAll(Collection<Script> entities) {
    entities.forEach(script ->
        inMemoryCachedEntities.getScriptMap().putIfAbsent(script.getHash(), script));
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    scriptRepository.saveAll(inMemoryCachedEntities.getScriptMap().values());
    inMemoryCachedEntities.getScriptMap().clear();
  }
}
