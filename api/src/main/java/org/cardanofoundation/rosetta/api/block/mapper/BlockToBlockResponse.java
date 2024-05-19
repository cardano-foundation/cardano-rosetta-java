package org.cardanofoundation.rosetta.api.block.mapper;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.BlockResponse;

@Mapper(config = BaseMapper.class, uses = {BlockTxToRosettaTransaction.class})
public interface BlockToBlockResponse {

  @Mapping(target = "block.blockIdentifier.hash", source = "hash")
  @Mapping(target = "block.blockIdentifier.index", source = "number")
  @Mapping(target = "block.parentBlockIdentifier.hash", source = "previousBlockHash")
  @Mapping(target = "block.parentBlockIdentifier.index", source = "previousBlockNumber")
  @Mapping(target = "block.timestamp", source = "createdAt")
  @Mapping(target = "block.metadata.transactionsCount", source = "transactionsCount")
  @Mapping(target = "block.metadata.createdBy", source = "createdBy")
  @Mapping(target = "block.metadata.size", source = "size")
  @Mapping(target = "block.metadata.slotNo", source = "slotNo")
  @Mapping(target = "block.metadata.epochNo", source = "epochNo")
  @Mapping(target = "block.transactions", source = "transactions", qualifiedByName = "toDto")
  BlockResponse toDto(Block model);
//
//  final ModelMapper modelMapper;
//  final BlockTxToRosettaTransaction mapToRosettaTransaction;
//
//
//  /**
//   * Maps a list of Block model to a Rosetta compatible BlockResponse.
//   *
//   * @param model The block from where the balances are calculated into the past
//   * @return The Rosetta compatible BlockResponse
//   */
//  public BlockResponse toDto(Block model) {
//    return modelMapper.typeMap(Block.class, BlockResponse.class)
//        .addMappings(mapper -> {
//          mapper.<String>map(Block::getHash, (dest, v) -> currentId(dest).setHash(v));
//          mapper.<Long>map(Block::getNumber, (dest, v) -> currentId(dest).setIndex(v));
//
//          mapper.<String>map(Block::getPreviousBlockHash, (dest, v) -> parentId(dest).setHash(v));
//          mapper.<Long>map(Block::getPreviousBlockNumber, (dest, v) -> parentId(dest).setIndex(v));
//
//          mapper.<Long>map(Block::getCreatedAt, (dest, v) -> dest.getBlock().setTimestamp(v));
//        })
//        .setPostConverter(ctx -> {
//          Block source = ctx.getSource();
//          ctx.getDestination().getBlock().setMetadata(
//              BlockMetadata.builder()
//                  .transactionsCount(source.getTransactionsCount())
//                  .createdBy(source.getCreatedBy())
//                  .size(source.getSize())
//                  .slotNo(source.getSlotNo())
//                  .epochNo(source.getEpochNo())
//                  .build()
//          );
//
//          ctx.getDestination().getBlock().setTransactions(
//              mapToRosettaTransactions(source.getTransactions())
//          );
//
//          return ctx.getDestination();
//        }).map(model);
//
//  }




//  /**
//   * Maps a list of Cardano Transactions to a list of Rosetta compatible Transactions.
//   *
//   * @param transactions The transactions to be mapped
//   * @return The list of Rosetta compatible Transactions
//   */
//  public List<Transaction> mapToRosettaTransactions(List<BlockTx> transactions) {
//    List<Transaction> rosettaTransactions = new ArrayList<>();
//    for (BlockTx tranDto : transactions) {
//      rosettaTransactions.add(mapToRosettaTransaction.toDto(tranDto));
//    }
//    return rosettaTransactions;
//  }
//
//  private static BlockIdentifier parentId(BlockResponse dest) {
//    return dest.getBlock().getParentBlockIdentifier();
//  }
//
//  private static BlockIdentifier currentId(BlockResponse dest) {
//    return dest.getBlock().getBlockIdentifier();
//  }
}
