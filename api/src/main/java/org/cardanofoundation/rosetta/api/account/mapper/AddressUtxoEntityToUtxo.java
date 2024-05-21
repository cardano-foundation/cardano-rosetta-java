package org.cardanofoundation.rosetta.api.account.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class)
public interface AddressUtxoEntityToUtxo {

  Utxo toDto(AddressUtxoEntity entity);
  void overWriteDto(@MappingTarget Utxo utxo, AddressUtxoEntity entity);
}
