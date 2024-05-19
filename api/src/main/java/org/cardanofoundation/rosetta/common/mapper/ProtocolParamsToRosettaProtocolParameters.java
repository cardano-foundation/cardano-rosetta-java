package org.cardanofoundation.rosetta.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.ProtocolParameters;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;

@Mapper(config = BaseMapper.class)
public interface ProtocolParamsToRosettaProtocolParameters {

  @Mapping(target = "coinsPerUtxoSize", source = "adaPerUtxoByte")
  @Mapping(target = "minFeeCoefficient", source = "minFeeA")
  @Mapping(target = "minFeeConstant", source = "minFeeB")
  @Mapping(target = "protocol", source = "protocolVersion.major")
  ProtocolParameters toProtocolParameters(ProtocolParams model);

}
