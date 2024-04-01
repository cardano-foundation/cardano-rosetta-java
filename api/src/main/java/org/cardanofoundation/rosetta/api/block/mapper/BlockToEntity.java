package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;

import static java.util.Optional.ofNullable;

@PersistenceMapper
@AllArgsConstructor
public class BlockToEntity {

  final ModelMapper modelMapper;


  public Block fromEntity(BlockEntity entity) {

    return ofNullable(modelMapper.getTypeMap(BlockEntity.class, Block.class))
        .orElseGet(() -> modelMapper.createTypeMap(BlockEntity.class, Block.class))
        .addMappings(mapper -> {

          mapper.map(BlockEntity::getIssuerVkey, Block::setCreatedBy);
          mapper.map(BlockEntity::getNoOfTxs, Block::setTransactionsCount);
          mapper.map(BlockEntity::getEpochNumber, Block::setEpochNo);
          mapper.map(BlockEntity::getSlot, Block::setSlotNo);

        })
        .setPostConverter(ctx -> {

          dest(ctx).setCreatedAt(TimeUnit.SECONDS.toMillis(source(ctx).getBlockTimeInSeconds()));

          dest(ctx).setPreviousBlockHash(
              ofNullable(source(ctx).getPrev())
                  .map(BlockEntity::getHash)
                  .orElse(source(ctx).getHash()));

          dest(ctx).setPreviousBlockNumber(
              ofNullable(source(ctx).getPrev())
                  .map(BlockEntity::getNumber)
                  .orElse(0L));

          dest(ctx).setSize(Math.toIntExact(source(ctx).getBlockBodySize()));
          dest(ctx).setTransactions(
              source(ctx).getTransactions().stream().map(Tran::fromTx).toList());

          return dest(ctx);

        }).map(entity);

  }

  private static BlockEntity source(MappingContext<BlockEntity, Block> ctx) {
    return ctx.getSource();
  }

  private static Block dest(MappingContext<BlockEntity, Block> ctx) {
    return ctx.getDestination();
  }

}
