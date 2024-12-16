package org.cardanofoundation.rosetta.common.mapper;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.openapitools.client.model.ProtocolParameters;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams.ProtocolVersion;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProtocolParamsToRosettaProtocolParamsTest extends BaseMapperSetup {

  @Autowired
  private ProtocolParamsMapper mapper;

  @Test
  void fromDomainObjectToRosettaTest() {
    ProtocolParams protocolParams = newProtocolParams();
    ProtocolParameters protocolParameters = mapper.mapToProtocolParameters(protocolParams);

    assertEquals(protocolParams.getAdaPerUtxoByte().toString(), protocolParameters.getCoinsPerUtxoSize());
    assertEquals(protocolParams.getMaxTxSize(), protocolParameters.getMaxTxSize());
    assertEquals(protocolParams.getMaxValSize(), protocolParameters.getMaxValSize());
    assertEquals(protocolParams.getKeyDeposit().toString(), protocolParameters.getKeyDeposit());
    assertEquals(protocolParams.getMaxCollateralInputs(), protocolParameters.getMaxCollateralInputs());
    assertEquals(protocolParams.getMinFeeA(), protocolParameters.getMinFeeCoefficient());
    assertEquals(protocolParams.getMinFeeB(), protocolParameters.getMinFeeConstant());
    assertEquals(protocolParams.getMinPoolCost().toString(), protocolParameters.getMinPoolCost());
    assertEquals(protocolParams.getPoolDeposit().toString(), protocolParameters.getPoolDeposit());
    assertEquals(protocolParams.getProtocolMajorVer(), protocolParameters.getProtocol());

  }

  private ProtocolParams newProtocolParams() {
    return ProtocolParams.builder()
        .adaPerUtxoByte(BigInteger.valueOf(1))
        .maxTxSize(2)
        .maxValSize(3L)
        .keyDeposit(BigInteger.valueOf(4))
        .maxCollateralInputs(5)
        .minFeeA(6)
        .minFeeB(7)
        .minPoolCost(BigInteger.valueOf(8))
        .poolDeposit(BigInteger.valueOf(9))
        .protocolMajorVer(10)
        .build();

  }
}
