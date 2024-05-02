package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.BlockIdentifier;
import org.openapitools.client.model.BlockMetadata;
import org.openapitools.client.model.BlockResponse;
import org.openapitools.client.model.Transaction;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

@OpenApiMapper
@AllArgsConstructor
public class BlockToBlockResponse {

  final ModelMapper modelMapper;
  final BlockTxToRosettaTransaction mapToRosettaTransaction;


  /**
   * Maps a list of Block model to a Rosetta compatible BlockResponse.
   *
   * @param model The block from where the balances are calculated into the past
   * @return The Rosetta compatible BlockResponse
   */
  public BlockResponse toDto(Block model) {
    return modelMapper.typeMap(Block.class, BlockResponse.class)
        .addMappings(mapper -> {
          mapper.<String>map(Block::getHash, (dest, v) -> currentId(dest).setHash(v));
          mapper.<Long>map(Block::getNumber, (dest, v) -> currentId(dest).setIndex(v));

          mapper.<String>map(Block::getPreviousBlockHash, (dest, v) -> parentId(dest).setHash(v));
          mapper.<Long>map(Block::getPreviousBlockNumber, (dest, v) -> parentId(dest).setIndex(v));

          mapper.<Long>map(Block::getCreatedAt, (dest, v) -> dest.getBlock().setTimestamp(v));
        })
        .setPostConverter(ctx -> {
          Block source = ctx.getSource();
          ctx.getDestination().getBlock().setMetadata(
              BlockMetadata.builder()
                  .transactionsCount(source.getTransactionsCount())
                  .createdBy(source.getCreatedBy())
                  .size(source.getSize())
                  .slotNo(source.getSlotNo())
                  .epochNo(source.getEpochNo())
                  .build()
          );

          ctx.getDestination().getBlock().setTransactions(
              mapToRosettaTransactions(source.getTransactions())
          );

          return ctx.getDestination();
        }).map(model);

  }




  /**
   * Maps a list of Cardano Transactions to a list of Rosetta compatible Transactions.
   *
   * @param transactions The transactions to be mapped
   * @return The list of Rosetta compatible Transactions
   */
  public List<Transaction> mapToRosettaTransactions(List<BlockTx> transactions) {
    List<Transaction> rosettaTransactions = new ArrayList<>();
    for (BlockTx tranDto : transactions) {
      rosettaTransactions.add(mapToRosettaTransaction.toDto(tranDto));
    }
    return rosettaTransactions;
  }

  private static BlockIdentifier parentId(BlockResponse dest) {
    return dest.getBlock().getParentBlockIdentifier();
  }

  private static BlockIdentifier currentId(BlockResponse dest) {
    return dest.getBlock().getBlockIdentifier();
  }
}
