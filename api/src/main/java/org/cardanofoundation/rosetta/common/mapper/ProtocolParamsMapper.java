package org.cardanofoundation.rosetta.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.ProtocolParameters;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class)
public interface ProtocolParamsMapper {

  @Mapping(target = "protocolVersion.major", source = "protocolMajorVer")
  @Mapping(target = "protocolVersion.minor", source = "protocolMinorVer")
  ProtocolParams mapProtocolParamsToEntity(ProtocolParamsEntity entity);

  @Mapping(target = "coinsPerUtxoSize", source = "adaPerUtxoByte")
  @Mapping(target = "minFeeCoefficient", source = "minFeeA")
  @Mapping(target = "minFeeConstant", source = "minFeeB")
  @Mapping(target = "protocol", source = "protocolVersion.major")
  ProtocolParameters mapToProtocolParameters(ProtocolParams model);
}
