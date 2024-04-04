package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
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

    return Optional
        .ofNullable(modelMapper.getTypeMap(Block.class, BlockResponse.class))
        .orElseGet(() -> modelMapper.createTypeMap(Block.class, BlockResponse.class))
        .addMappings(mapper -> {
          mapper.<String>map(Block::getHash, (dest, v) -> currentId(dest).setHash(v));
          mapper.<Long>map(Block::getNumber, (dest, v) -> currentId(dest).setIndex(v));

          mapper.<String>map(Block::getPreviousBlockHash, (dest, v) -> parentId(dest).setHash(v));
          mapper.<Long>map(Block::getPreviousBlockNumber, (dest, v) -> parentId(dest).setIndex(v));

          mapper.<Long>map(Block::getCreatedAt, (dest, v) -> dest.getBlock().setTimestamp(v));
        })
        .setPostConverter(ctx -> {
          ctx.getDestination().getBlock().setMetadata(
              BlockMetadata.builder()
                  .transactionsCount(source(ctx).getTransactionsCount())
                  .createdBy(source(ctx).getCreatedBy())
                  .size(source(ctx).getSize())
                  .epochNo(source(ctx).getEpochNo())
                  .slotNo(source(ctx).getSlotNo())
                  .build()
          );

          ctx.getDestination().getBlock().setTransactions(
              mapToRosettaTransactions(source(ctx).getTransactions(), source(ctx).getPoolDeposit())
          );

          return ctx.getDestination();
        }).map(model);

  }

  private static Block source(MappingContext<Block, BlockResponse> ctx) {
    return ctx.getSource();
  }


  /**
   * Maps a list of Cardano Transactions to a list of Rosetta compatible Transactions.
   *
   * @param transactions The transactions to be mapped
   * @param poolDeposit  The pool deposit
   * @return The list of Rosetta compatible Transactions
   */
  public List<Transaction> mapToRosettaTransactions(
      List<BlockTx> transactions,
      String poolDeposit) {
    List<Transaction> rosettaTransactions = new ArrayList<>();
    for (BlockTx tranDto : transactions) {
      rosettaTransactions.add(mapToRosettaTransaction.toDto(tranDto, poolDeposit));
    }
    return rosettaTransactions;
  }

//  /**
//   * Maps a TransactionDto to a Rosetta compatible BlockTx.
//   *
//   * @param transaction The Cardano transaction to be mapped
//   * @param poolDeposit The pool deposit
//   * @return The Rosetta compatible Transaction
//   */
//  public static Transaction mapToRosettaTransaction(BlockTx transaction, String poolDeposit) {
//    Transaction rosettaTransaction = new Transaction();
//    TransactionIdentifier identifier = new TransactionIdentifier();
//    identifier.setHash(transaction.getHash());
//    rosettaTransaction.setTransactionIdentifier(identifier);
//
//    OperationStatus status = new OperationStatus();
////    status.setStatus(Boolean.TRUE.equals(transaction.getValidContract()) ? SUCCESS_OPERATION_STATUS.getStatus() : INVALID_OPERATION_STATUS.getStatus());
//    status.setStatus(SUCCESS_OPERATION_STATUS.getStatus()); // TODO need to check the right status
//    List<Operation> operations =
//        OperationDataMapper.getAllOperations(transaction, poolDeposit, status);
//
//    rosettaTransaction.setMetadata(TransactionMetadata.builder()
//        .size(transaction.getSize()) // Todo size is not available
//        .scriptSize(transaction.getScriptSize()) // TODO script size is not available
//        .build());
//    rosettaTransaction.setOperations(operations);
//    return rosettaTransaction;
//
//  }

  private static BlockIdentifier parentId(BlockResponse dest) {
    return dest.getBlock().getParentBlockIdentifier();
  }

  private static BlockIdentifier currentId(BlockResponse dest) {
    return dest.getBlock().getBlockIdentifier();
  }


}
