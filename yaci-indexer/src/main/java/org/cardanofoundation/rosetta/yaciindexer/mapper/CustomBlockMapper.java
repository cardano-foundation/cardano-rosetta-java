package org.cardanofoundation.rosetta.yaciindexer.mapper;

import com.bloxbean.cardano.yaci.store.blocks.domain.Block;
import com.bloxbean.cardano.yaci.store.blocks.storage.impl.mapper.BlockMapperImpl;
import com.bloxbean.cardano.yaci.store.blocks.storage.impl.model.BlockEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CustomBlockMapper extends BlockMapperImpl {

  @Override
  public BlockEntity toBlockEntity(Block blockDetails) {
    return BlockEntity.builder()
        .hash(blockDetails.getHash())
        .number(blockDetails.getNumber())
        .slot(blockDetails.getSlot())
        .epochNumber(blockDetails.getEpochNumber())
        .blockTime(blockDetails.getBlockTime())
        .prevHash(blockDetails.getPrevHash())
        .blockBodySize(blockDetails.getBlockBodySize())
        .noOfTxs(blockDetails.getNoOfTxs())
        .slotLeader(blockDetails.getSlotLeader())
        .build();
  }
}