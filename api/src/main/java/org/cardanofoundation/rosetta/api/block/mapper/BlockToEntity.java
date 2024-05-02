package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;

import static java.util.Optional.ofNullable;

@PersistenceMapper
@AllArgsConstructor
public class BlockToEntity {

  final ModelMapper modelMapper;

  final BlockTxToEntity blockTxToEntity;


  public Block fromEntity(BlockEntity entity) {

    return modelMapper.typeMap(BlockEntity.class, Block.class)
        .addMappings(mapper -> {

          mapper.map(BlockEntity::getSlotLeader, Block::setCreatedBy);
          mapper.map(BlockEntity::getNoOfTxs, Block::setTransactionsCount);
          mapper.map(BlockEntity::getSlot, Block::setSlotNo);
          mapper.map(BlockEntity::getEpochNumber, Block::setEpochNo);

        })
        .setPostConverter(ctx -> {
          BlockEntity source = ctx.getSource();
          Block dest = ctx.getDestination();

          dest.setCreatedAt(TimeUnit.SECONDS.toMillis(source.getBlockTimeInSeconds()));
          dest.setSize(Math.toIntExact(source.getBlockBodySize()));

          dest.setPreviousBlockHash(ofNullable(source.getPrev())
              .map(BlockEntity::getHash)
              .orElse(source.getHash()));

          dest.setPreviousBlockNumber(ofNullable(source.getPrev())
              .map(BlockEntity::getNumber)
              .orElse(0L));

          dest.setTransactions(source.getTransactions()
              .stream()
              .map(blockTxToEntity::fromEntity)
              .toList());

          return dest;
        }).map(entity);
  }

}
