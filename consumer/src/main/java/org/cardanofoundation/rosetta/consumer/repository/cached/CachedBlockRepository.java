package org.cardanofoundation.rosetta.consumer.repository.cached;

import com.sotatek.cardano.common.entity.Block;
import java.util.Optional;

public interface CachedBlockRepository extends BaseCachedRepository<Block> {

  Optional<Block> findBlockByHash(String hash);

  Boolean existsBlockByHash(String hash);


}
