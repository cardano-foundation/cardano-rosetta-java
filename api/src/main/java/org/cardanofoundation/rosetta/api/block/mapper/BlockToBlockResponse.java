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
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.TransactionIdentifier;
import org.openapitools.client.model.TransactionMetadata;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

import static org.cardanofoundation.rosetta.common.util.RosettaConstants.SUCCESS_OPERATION_STATUS;

@OpenApiMapper
@AllArgsConstructor
public class BlockToBlockResponse {

  final ModelMapper modelMapper;

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
   * Maps a list of TransactionDtos to a list of Rosetta compatible Transactions.
   *
   * @param transactions The transactions to be mapped
   * @param poolDeposit  The pool deposit
   * @return The list of Rosetta compatible Transactions
   */
  public static List<org.openapitools.client.model.Transaction> mapToRosettaTransactions(
      List<org.cardanofoundation.rosetta.api.block.model.domain.Transaction> transactions,
      String poolDeposit) {
    List<org.openapitools.client.model.Transaction> rosettaTransactions = new ArrayList<>();
    for (org.cardanofoundation.rosetta.api.block.model.domain.Transaction transactionDto : transactions) {
      rosettaTransactions.add(mapToRosettaTransaction(transactionDto, poolDeposit));
    }
    return rosettaTransactions;
  }

  /**
   * Maps a TransactionDto to a Rosetta compatible Transaction.
   *
   * @param transactionDto The transaction to be mapped
   * @param poolDeposit    The pool deposit
   * @return The Rosetta compatible Transaction
   */
  public static org.openapitools.client.model.Transaction mapToRosettaTransaction(
      org.cardanofoundation.rosetta.api.block.model.domain.Transaction transactionDto,
      String poolDeposit) {
    org.openapitools.client.model.Transaction rosettaTransaction = new org.openapitools.client.model.Transaction();
    TransactionIdentifier identifier = new TransactionIdentifier();
    identifier.setHash(transactionDto.getHash());
    rosettaTransaction.setTransactionIdentifier(identifier);

    OperationStatus status = new OperationStatus();
//    status.setStatus(Boolean.TRUE.equals(transactionDto.getValidContract()) ? SUCCESS_OPERATION_STATUS.getStatus() : INVALID_OPERATION_STATUS.getStatus());
    status.setStatus(SUCCESS_OPERATION_STATUS.getStatus()); // TODO need to check the right status
    List<Operation> operations = OperationDataMapper.getAllOperations(transactionDto, poolDeposit,
        status);

    rosettaTransaction.setMetadata(TransactionMetadata.builder()
        .size(transactionDto.getSize()) // Todo size is not available
        .scriptSize(transactionDto.getScriptSize()) // TODO script size is not available
        .build());
    rosettaTransaction.setOperations(operations);
    return rosettaTransaction;

  }

  private static BlockIdentifier parentId(BlockResponse dest) {
    return dest.getBlock().getParentBlockIdentifier();
  }

  private static BlockIdentifier currentId(BlockResponse dest) {
    return dest.getBlock().getBlockIdentifier();
  }


}
