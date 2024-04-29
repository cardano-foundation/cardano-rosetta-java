package org.cardanofoundation.rosetta.common.services.impl;

import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.block.model.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsToEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ProtocolParamServiceImplTest {

  ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  EpochParamRepository epochParamRepository;
  @Mock
  ProtocolParamsToEntity protocolParamsToEntity;

  ProtocolParamServiceImpl genesisService =
      new ProtocolParamServiceImpl(objectMapper,epochParamRepository,protocolParamsToEntity);

  @Test
  void getProtocolParameters() {

    String genesisPath = "../config/preprod/shelley-genesis.json";
    ReflectionTestUtils.setField(genesisService, "genesisShelleyPath", genesisPath);

    assertNotNull(genesisService.getProtocolParameters().getPoolDeposit());


  }
}