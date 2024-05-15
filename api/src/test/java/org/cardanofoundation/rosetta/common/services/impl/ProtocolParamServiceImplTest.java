package org.cardanofoundation.rosetta.common.services.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsToEntity;
import org.cardanofoundation.rosetta.common.services.ProtocolParamServiceImpl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtocolParamServiceImplTest {

  @Mock
  EpochParamRepository epochParamRepository;
  @Mock
  ProtocolParamsToEntity protocolParamsToEntity;
  @InjectMocks
  ProtocolParamServiceImpl genesisService;

  @Test
  void getProtocolParameters() {
    when(genesisService.findProtocolParametersFromIndexer()).thenReturn(new ProtocolParams());
    assertNotNull(genesisService.findProtocolParametersFromIndexer());
  }
}
