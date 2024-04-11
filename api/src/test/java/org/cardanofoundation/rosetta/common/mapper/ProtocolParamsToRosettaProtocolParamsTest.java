package org.cardanofoundation.rosetta.common.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import org.cardanofoundation.rosetta.api.BaseMapperTest;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams.ProtocolVersion;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.ProtocolParameters;
import org.springframework.beans.factory.annotation.Autowired;

public class ProtocolParamsToRosettaProtocolParamsTest extends BaseMapperTest {

  @Autowired
  private ProtocolParamsToRosettaProtocolParameters mapper;

  @Test
  public void fromDomainObjectToRosettaTest() {
    ProtocolParams protocolParams = newProtocolParams();
    ProtocolParameters protocolParameters = mapper.toProtocolParameters(protocolParams);

    assertEquals(protocolParams.getAdaPerUtxoByte().toString(), protocolParameters.getCoinsPerUtxoSize());
    assertEquals(protocolParams.getMaxTxSize(), protocolParameters.getMaxTxSize());
    assertEquals(protocolParams.getMaxValSize(), protocolParameters.getMaxValSize());
    assertEquals(protocolParams.getKeyDeposit().toString(), protocolParameters.getKeyDeposit());
    assertEquals(protocolParams.getMaxCollateralInputs(), protocolParameters.getMaxCollateralInputs());
    assertEquals(protocolParams.getMinFeeA(), protocolParameters.getMinFeeCoefficient());
    assertEquals(protocolParams.getMinFeeB(), protocolParameters.getMinFeeConstant());
    assertEquals(protocolParams.getMinPoolCost().toString(), protocolParameters.getMinPoolCost());
    assertEquals(protocolParams.getPoolDeposit().toString(), protocolParameters.getPoolDeposit());
    assertEquals(protocolParams.getProtocolVersion().getMajor(), protocolParameters.getProtocol());

  }

  private ProtocolParams newProtocolParams() {
    ProtocolVersion protocolVersion = new ProtocolVersion();
    protocolVersion.setMajor(10);
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
        .protocolVersion(protocolVersion)
        .build();

  }
}
