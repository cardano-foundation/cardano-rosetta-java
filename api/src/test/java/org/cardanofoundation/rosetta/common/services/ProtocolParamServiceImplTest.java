package org.cardanofoundation.rosetta.common.services;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsToEntity;

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

  @Test
  void test() {
    //given
    ProtocolParamsEntity protocolParams = new ProtocolParamsEntity();
    when(epochParamRepository.findLatestProtocolParams()).thenReturn(protocolParams);
    when(protocolParamsToEntity.fromEntity(protocolParams)).thenReturn(new ProtocolParams());
    //when
    ProtocolParams protocolParameters = genesisService.getProtocolParameters();
    //then
    assertNotNull(protocolParameters);
  }
}
