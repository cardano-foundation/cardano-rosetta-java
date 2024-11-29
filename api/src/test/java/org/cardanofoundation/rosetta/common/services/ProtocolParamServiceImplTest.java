package org.cardanofoundation.rosetta.common.services;

import java.math.BigInteger;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.LocalProtocolParamsEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.LocalProtocolParamsRepository;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtocolParamServiceImplTest {

  @Mock
  LocalProtocolParamsRepository protocolParamsRepository;
  @InjectMocks
  ProtocolParamServiceImpl genesisService;

  @Test
  void findProtocolParametersFromIndexerTest() {
    //given
    when(protocolParamsRepository.getLocalProtocolParams()).thenReturn(Optional.of(
        LocalProtocolParamsEntity.builder()
            .epoch(5L)
            .protocolParams(
                ProtocolParams.builder().build())
            .build()));
    //when
    ProtocolParams protocolParameters = genesisService.findProtocolParameters();
    //then
    assertNotNull(protocolParameters);
  }

  @Test
  void findProtocolParametersFromFile() {
    ReflectionTestUtils.setField(genesisService, "offlineMode", true);
    ReflectionTestUtils.setField(genesisService, "genesisShelleyPath", "src/test/resources/network-config/shelley-genesis.json");
    ReflectionTestUtils.setField(genesisService, "genesisAlonzoPath", "src/test/resources/network-config/alonzo-genesis.json");
    ReflectionTestUtils.setField(genesisService, "genesisConwayPath", "src/test/resources/network-config/conway-genesis.json");

    ProtocolParams protocolParameters = genesisService.findProtocolParameters();

    assertNotNull(protocolParameters);
    assertEquals(44, protocolParameters.getMinFeeA());
    assertEquals(155381, protocolParameters.getMinFeeB());
    assertEquals(BigInteger.valueOf(500000000L), protocolParameters.getPoolDeposit());
    assertEquals(BigInteger.valueOf(2000000L), protocolParameters.getKeyDeposit());
    assertEquals(BigInteger.valueOf(340000000L), protocolParameters.getMinPoolCost());
    assertEquals(BigInteger.valueOf(34482L), protocolParameters.getAdaPerUtxoByte());
    assertEquals(5000L, protocolParameters.getMaxValSize());
    assertEquals(3, protocolParameters.getMaxCollateralInputs());
  }
}
