package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.api.block.model.entity.projection.BlockIdentifierProjection;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;
import org.cardanofoundation.rosetta.common.mapper.util.OperationMapperService;

@Mapper(config = BaseMapper.class, uses = { OperationMapperService.class })
public interface BlockMapper {

  BlockIdentifierExtended mapToBlockIdentifierExtended(BlockIdentifierProjection projection);

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
  @Mapping(target = "block.transactions", source = "transactions")
  BlockResponse mapToBlockResponse(Block model);

  @Mapping(target = "block.blockIdentifier.hash", source = "model.hash")
  @Mapping(target = "block.blockIdentifier.index", source = "model.number")
  @Mapping(target = "block.parentBlockIdentifier.hash", source = "model.previousBlockHash")
  @Mapping(target = "block.parentBlockIdentifier.index", source = "model.previousBlockNumber")
  @Mapping(target = "block.timestamp", source = "model.createdAt")
  @Mapping(target = "block.metadata.transactionsCount", source = "model.transactionsCount")
  @Mapping(target = "block.metadata.createdBy", source = "model.createdBy")
  @Mapping(target = "block.metadata.size", source = "model.size")
  @Mapping(target = "block.metadata.slotNo", source = "model.slotNo")
  @Mapping(target = "block.metadata.epochNo", source = "model.epochNo")
  @Mapping(target = "block.transactions", source = "model.transactions", qualifiedByName = "mapToRosettaTransactionWithMetadata")
  BlockResponse mapToBlockResponseWithMetadata(Block model, @Context Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap);

  @Named("mapToBlockTransactionWithMetadata")
  @Mapping(target = "blockIdentifier", source = "source")
  @Mapping(target = "transaction", source = "source", qualifiedByName = "mapToRosettaTransactionWithMetadata")
  BlockTransaction mapToBlockTransactionWithMetadata(BlockTx source, @Context Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap);

  @Mapping(target = "hash", source = "blockHash")
  @Mapping(target = "index", source = "blockNo")
  BlockIdentifier mapToBlockIdentifier(BlockTx source);

  
  @Named("mapToRosettaTransactionWithMetadata")
  @Mapping(target = "transactionIdentifier", source = "source.hash")
  @Mapping(target = "metadata.size", source = "source.size")
  @Mapping(target = "metadata.scriptSize", source = "source.scriptSize")
  @Mapping(target = "operations", source = "source", qualifiedByName = "mapTransactionsToOperationsWithMetadata")
  Transaction mapToRosettaTransactionWithMetadata(BlockTx source, @Context Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap);

  @Mapping(target = "hash", source = "txHash")
  @Mapping(target = "blockHash", source = "block.hash")
  @Mapping(target = "blockNo", source = "block.number")
  @Mapping(target = "size", source = "sizeEntity.size")
  @Mapping(target = "scriptSize", source = "sizeEntity.scriptSize")
  @Mapping(target = "inputs", source = "inputKeys")
  @Mapping(target = "outputs", source = "outputKeys")
  BlockTx mapToBlockTx(TxnEntity model);

  @Mapping(target = "previousBlockHash", source = "prev.hash", defaultExpression = "java(entity.getHash())")
  @Mapping(target = "previousBlockNumber", source = "prev.number", defaultValue = "0L")
  @Mapping(target = "transactionsCount", source = "noOfTxs")
  @Mapping(target = "epochNo", source = "epochNumber")
  @Mapping(target = "createdBy", source = "slotLeader")
  @Mapping(target = "createdAt", source = "blockTimeInSeconds", qualifiedByName = "toMillis")
  @Mapping(target = "size", source = "blockBodySize")
  @Mapping(target = "slotNo", source = "slot")
  Block mapToBlock(BlockEntity entity);

  @Mapping(target = "transaction", source = "model")
  BlockTransactionResponse mapToBlockTransactionResponse(BlockTx model);
  
  @Mapping(target = "transaction", source = "model", qualifiedByName = "mapToRosettaTransactionWithMetadata")
  BlockTransactionResponse mapToBlockTransactionResponseWithMetadata(BlockTx model,
                                                                     @Context Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap);

  @Mapping(target = "transactionIdentifier", source = "hash")
  @Mapping(target = "metadata.size", source = "size")
  @Mapping(target = "metadata.scriptSize", source = "scriptSize")
  @Mapping(target = "operations", ignore = true)
  Transaction mapToRosettaTransactionBasic(BlockTx source);

  TransactionIdentifier getTransactionIdentifier(String hash);

  Utxo getUtxoFromUtxoKey(UtxoKey model);

  @Named("toMillis")
  default Long toMillis(Long seconds) {
    return TimeUnit.SECONDS.toMillis(seconds);
  }
}
