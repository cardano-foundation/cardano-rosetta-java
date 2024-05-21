package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class, uses = {UtxoKeyToEntity.class})
public interface BlockTxToEntity {

  @Mapping(target = "hash", source = "txHash")
  @Mapping(target = "blockHash", source = "block.hash")
  @Mapping(target = "blockNo", source = "block.number")
  @Mapping(target = "size", constant = "0L")
  @Mapping(target = "scriptSize", constant = "0L")
  @Mapping(target = "inputs", source = "inputKeys")
  @Mapping(target = "outputs", source = "outputKeys")
  BlockTx fromEntity(TxnEntity model);

}
