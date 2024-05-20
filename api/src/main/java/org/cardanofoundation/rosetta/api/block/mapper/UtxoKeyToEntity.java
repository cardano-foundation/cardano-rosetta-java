package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class)
public interface UtxoKeyToEntity {

  Utxo fromEntity(UtxoKey model);

}
