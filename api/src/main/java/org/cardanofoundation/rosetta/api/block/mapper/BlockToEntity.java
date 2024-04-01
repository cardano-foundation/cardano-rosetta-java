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
        .setPostConverter(ctx -> {
          dest(ctx)
              .setCreatedAt(TimeUnit.SECONDS.toMillis(src(ctx).getBlockTimeInSeconds()));

          dest(ctx).setPreviousBlockHash(
              ofNullable(src(ctx).getPrev())
                  .map(BlockEntity::getHash)
                  .orElse(src(ctx).getHash()));

          dest(ctx).setPreviousBlockNumber(
              ofNullable(src(ctx).getPrev())
                  .map(BlockEntity::getNumber)
                  .orElse(0L));

          dest(ctx).setTransactionsCount(src(ctx).getNoOfTxs());
          dest(ctx).setSize(Math.toIntExact(src(ctx).getBlockBodySize()));
          dest(ctx).setCreatedBy(src(ctx).getIssuerVkey());
          dest(ctx).setEpochNo(src(ctx).getEpochNumber());
          dest(ctx).setSlotNo(src(ctx).getSlot());
          dest(ctx).setTransactions(src(ctx).getTransactions().stream().map(Tran::fromTx).toList());

          return dest(ctx);

        }).map(entity);

  }

  private static BlockEntity src(MappingContext<BlockEntity, Block> ctx) {
    return ctx.getSource();
  }

  private static Block dest(MappingContext<BlockEntity, Block> ctx) {
    return ctx.getDestination();
  }

}
