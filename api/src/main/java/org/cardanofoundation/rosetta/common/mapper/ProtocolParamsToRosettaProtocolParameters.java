package org.cardanofoundation.rosetta.common.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.ProtocolParameters;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

@OpenApiMapper
@AllArgsConstructor
public class ProtocolParamsToRosettaProtocolParameters {

  final ModelMapper modelMapper;

  // TODO need to be removed, since it's much slower -> 18ms
//  public ProtocolParameters toProtocolParameters(ProtocolParams model) {
//    return modelMapper.typeMap(ProtocolParams.class, ProtocolParameters.class)
//        .addMappings(mapper -> {
//          mapper.map(ProtocolParams::getAdaPerUtxoByte, ProtocolParameters::setCoinsPerUtxoSize);
//          mapper.map(ProtocolParams::getMaxCollateralInputs,
//              ProtocolParameters::setMaxCollateralInputs);
//          mapper.map(ProtocolParams::getMinFeeA, ProtocolParameters::setMinFeeCoefficient);
//          mapper.map(ProtocolParams::getMinFeeB, ProtocolParameters::setMinFeeConstant);
//          mapper.map(source -> source.getProtocolVersion().getMajor(),
//              ProtocolParameters::setProtocol);
//        })
//        .map(model);
//  }

  public ProtocolParameters toProtocolParameters(ProtocolParams model) {
    return ProtocolParameters.builder()
        .coinsPerUtxoSize(model.getAdaPerUtxoByte().toString())
        .maxTxSize(model.getMaxTxSize())
        .maxValSize(model.getMaxValSize())
        .keyDeposit(model.getKeyDeposit().toString())
        .maxCollateralInputs(model.getMaxCollateralInputs())
        .minFeeCoefficient(model.getMinFeeA())
        .minFeeConstant(model.getMinFeeB())
        .poolDeposit(model.getPoolDeposit().toString())
        .minPoolCost(model.getMinPoolCost().toString())
        .protocol(model.getProtocolVersion().getMajor())
        .build();
  }
}
