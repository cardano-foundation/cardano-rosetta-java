package org.cardanofoundation.rosetta.common.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams.ProtocolVersion;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;

@PersistenceMapper
@AllArgsConstructor
public class ProtocolParamsToEntity {

  final ModelMapper modelMapper;

  // TODO need to be removed - These mapper slow down our response times
//  public ProtocolParams fromEntity(ProtocolParamsEntity entity){
//    return modelMapper.typeMap(ProtocolParamsEntity.class, ProtocolParams.class)
//        .implicitMappings()
//        .addMappings(mapper -> {
//          mapper.<Integer>map(ProtocolParamsEntity::getProtocolMajorVer, (dest, v) -> dest.getProtocolVersion().setMajor(v));
//          mapper.<Integer>map(ProtocolParamsEntity::getProtocolMinorVer, (dest, v) -> dest.getProtocolVersion().setMinor(v));
//        })
//        .map(entity);
//  }

  public ProtocolParams fromEntity(ProtocolParamsEntity entity) {
    ProtocolVersion protocolVersion = new ProtocolVersion();
    protocolVersion.setMajor(entity.getProtocolMajorVer());
    protocolVersion.setMinor(entity.getProtocolMinorVer());
    return ProtocolParams.builder()
        .minFeeA(entity.getMinFeeA())
        .minFeeB(entity.getMinFeeB())
        .maxTxSize(entity.getMaxTxSize())
        .keyDeposit(entity.getKeyDeposit())
        .poolDeposit(entity.getPoolDeposit())
        .protocolVersion(protocolVersion)
        .minPoolCost(entity.getMinPoolCost())
        .adaPerUtxoByte(entity.getAdaPerUtxoByte())
        .costModels(entity.getCostModels())
        .maxValSize(entity.getMaxValSize())
        .maxCollateralInputs(entity.getMaxCollateralInputs())
        .build();
  }

  public ProtocolParams merge(ProtocolParams from, ProtocolParams to){
    ModelMapper notNullMapper = new ModelMapper();
    notNullMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    notNullMapper.map(from, to);
    return to;
  }

}
