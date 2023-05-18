package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedSlotLeader;
import org.cardanofoundation.rosetta.common.entity.PoolHash;
import org.cardanofoundation.rosetta.common.entity.SlotLeader;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.common.ledgersync.Block;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronMainBlock;
import org.cardanofoundation.rosetta.common.util.SlotLeaderUtils;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolHashRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedSlotLeaderRepository;
import org.cardanofoundation.rosetta.consumer.service.SlotLeaderService;
import java.math.BigInteger;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlotLeaderServiceImpl implements SlotLeaderService {

  public static final int HASH_LENGTH = 16;
  public static final String DELIMITER = "-";
  CachedSlotLeaderRepository slotLeaderRepository;
  CachedPoolHashRepository cachedPoolHashRepository;


  @Override
  public AggregatedSlotLeader getSlotLeaderHashAndPrefix(Block blockCddl) {
    String issuerVkey = blockCddl.getHeader().getHeaderBody().getIssuerVkey();
    String hashRaw = SlotLeaderUtils.getAfterByronSlotLeader(issuerVkey);
    return new AggregatedSlotLeader(hashRaw, ConsumerConstant.SHELLEY_SLOT_LEADER_PREFIX);
  }

  @Override
  public AggregatedSlotLeader getSlotLeaderHashAndPrefix(ByronMainBlock blockCddl) {
    String pubKey = blockCddl.getHeader().getConsensusData().getPubKey();
    String hashRaw = SlotLeaderUtils.getByronSlotLeader(pubKey);
    return new AggregatedSlotLeader(hashRaw, ConsumerConstant.BYRON_SLOT_LEADER_PREFIX);
  }

  @Override
  public SlotLeader getSlotLeader(String hashRaw, String prefix) {
    Optional<SlotLeader> slotLeaderOptional = slotLeaderRepository.findSlotLeaderByHash(hashRaw);

    if (slotLeaderOptional.isEmpty()) {
      Optional<PoolHash> poolHashOptional = cachedPoolHashRepository.findPoolHashByHashRaw(hashRaw);

      if (poolHashOptional.isEmpty()) {
        SlotLeader slotLeader = buildSlotLeader(hashRaw, prefix, null);
        slotLeaderRepository.save(slotLeader);
        return slotLeader;
      }

      SlotLeader slotLeader = buildSlotLeader(hashRaw,
          ConsumerConstant.POOL_HASH_PREFIX,
          poolHashOptional.get());
      slotLeaderRepository.save(slotLeader);
      return slotLeader;
    }

    return slotLeaderOptional.get();
  }

  private static SlotLeader buildSlotLeader(String hashRaw, String prefix, PoolHash poolHash) {
    return SlotLeader.builder()
        .poolHash(poolHash)
        .hash(hashRaw)
        .description(String.join
            (DELIMITER,
                prefix,
                hashRaw.substring(BigInteger.ZERO.intValue(),
                    HASH_LENGTH)))
        .build();
  }
}
