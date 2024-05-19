package org.cardanofoundation.rosetta.common.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;


@Mapper(config = BaseMapper.class)
public interface ProtocolParamsToEntity {

  @Mapping(target = "protocolVersion.major", source = "protocolMajorVer")
  @Mapping(target = "protocolVersion.minor", source = "protocolMinorVer")
  ProtocolParams fromEntity(ProtocolParamsEntity entity);

}
