package org.cardanofoundation.rosetta.common.services.impl;

import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenesisServiceImplTest {

  ObjectMapper objectMapper = new ObjectMapper();
  GenesisServiceImpl genesisService = new GenesisServiceImpl(objectMapper);

  @Test
  void getProtocolParameters() {

    String genesisPath = "../config/preprod/shelley-genesis.json";
    ReflectionTestUtils.setField(genesisService, "genesisShelleyPath", genesisPath);

    assertNotNull(genesisService.getProtocolParameters().getPoolDeposit());


  }
}