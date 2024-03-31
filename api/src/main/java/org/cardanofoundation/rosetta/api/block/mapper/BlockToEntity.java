package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;

@PersistenceMapper
@AllArgsConstructor
public class BlockToEntity {

  final ModelMapper modelMapper;


  public Block fromEntity(BlockEntity entity) {

//    return Optional.ofNullable(modelMapper.getTypeMap(BlockEntity.class, Block.class))
//        .orElseGet(() -> modelMapper.createTypeMap(BlockEntity.class, Block.class))
//        .setPostConverter(ctx -> {
//          ctx.getDestination()
//              .setCreatedAt(TimeUnit.SECONDS.toMillis(ctx.getSource().getBlockTimeInSeconds()));
//
//          ctx.getDestination().setSlotNo(ctx.getSource().getSlot());
//          ctx.getDestination().setTransactions(ctx.getSource().getTransactions().stream().map(Tran::fromTx).toList());
//          ctx.getDestination().setTransactions(List.of());
//
//
//          return ctx.getDestination();
//
//        }).map(entity);
//
    return Block.builder()
        .number(entity.getNumber())
        .hash(entity.getHash())
        .createdAt(TimeUnit.SECONDS.toMillis(entity.getBlockTimeInSeconds()))
        .previousBlockHash(entity.getPrev() != null ? entity.getPrev().getHash()
            : entity.getHash()) // TODO EPAM: check for genesis entity
        .previousBlockNumber(entity.getPrev() != null ? entity.getPrev().getNumber() : 0)
        .transactionsCount(entity.getNoOfTxs())
        .size(Math.toIntExact(entity.getBlockBodySize()))
        .createdBy(
            entity.getIssuerVkey()) // TODO probably need to change this, in typescript rosetta there is something like Pool-[HASH]
        .epochNo(entity.getEpochNumber())
        .slotNo(entity.getSlot())
        .transactions(entity.getTransactions().stream().map(Tran::fromTx).toList())
        .build();
  }

}
