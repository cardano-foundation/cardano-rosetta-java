package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.SlotLeader;
import java.util.Optional;

public interface CachedSlotLeaderRepository extends BaseCachedRepository<SlotLeader> {

  Optional<SlotLeader> findSlotLeaderByHash(String hash);
}
