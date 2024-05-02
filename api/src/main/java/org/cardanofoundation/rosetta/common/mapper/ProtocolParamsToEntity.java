package org.cardanofoundation.rosetta.common.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;

@PersistenceMapper
@AllArgsConstructor
public class ProtocolParamsToEntity {

  final ModelMapper modelMapper;

  public ProtocolParams fromEntity(ProtocolParamsEntity entity){
    return modelMapper.typeMap(ProtocolParamsEntity.class, ProtocolParams.class)
        .implicitMappings()
        .addMappings(mapper -> {
          mapper.<String>map(ProtocolParamsEntity::getExtraEntropy, (dest, v) -> dest.getExtraEntropy().setTag(v));
          mapper.map(ProtocolParamsEntity::getMaxBlockSize, ProtocolParams::setMaxBlockBodySize);
          mapper.<Integer>map(ProtocolParamsEntity::getProtocolMajorVer, (dest, v) -> dest.getProtocolVersion().setMajor(v));
          mapper.<Integer>map(ProtocolParamsEntity::getProtocolMinorVer, (dest, v) -> dest.getProtocolVersion().setMinor(v));
        })
        .map(entity);
  }

  public ProtocolParams merge(ProtocolParams from, ProtocolParams to){
    ModelMapper notNullMapper = new ModelMapper();
    notNullMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    notNullMapper.map(from, to);
    return to;
  }

}
