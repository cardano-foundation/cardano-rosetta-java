package org.cardanofoundation.rosetta.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.ProtocolParameters;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;

@Mapper(config = BaseMapper.class)
public interface ProtocolParamsMapper {

  ProtocolParams mapProtocolParamsToEntity(ProtocolParamsEntity entity);

  @Mapping(target = "coinsPerUtxoSize", source = "adaPerUtxoByte")
  @Mapping(target = "minFeeCoefficient", source = "minFeeA")
  @Mapping(target = "minFeeConstant", source = "minFeeB")
  @Mapping(target = "protocol", source = "protocolMajorVer")
  ProtocolParameters mapToProtocolParameters(ProtocolParams model);
}
