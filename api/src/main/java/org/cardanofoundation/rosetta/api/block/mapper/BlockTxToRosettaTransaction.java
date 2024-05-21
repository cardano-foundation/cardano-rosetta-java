package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.client.model.Transaction;
import org.openapitools.client.model.TransactionIdentifier;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;
import org.cardanofoundation.rosetta.common.mapper.util.MapperUtils;
import org.cardanofoundation.rosetta.common.mapper.util.OperationMapperUtils;

@Mapper(config = BaseMapper.class, uses = {MapperUtils.class, OperationMapperUtils.class})
public interface BlockTxToRosettaTransaction {

  @Mapping(target = "transactionIdentifier", source = "hash", qualifiedByName = "getTransactionIdentifier")
  @Mapping(target = "metadata.size", source = "size")
  @Mapping(target = "metadata.scriptSize", source = "scriptSize")
  @Mapping(target = "operations", source = "source", qualifiedByName = "mapTransactionsToOperations")
  Transaction toDto(BlockTx source);

  @Named("getTransactionIdentifier")
  default TransactionIdentifier getTransactionIdentifier(String hash) {
    return TransactionIdentifier.builder().hash(hash).build();
  }

}
