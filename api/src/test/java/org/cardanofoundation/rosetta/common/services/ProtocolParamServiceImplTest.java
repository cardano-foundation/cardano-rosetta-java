package org.cardanofoundation.rosetta.common.services;

import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.LocalProtocolParamsEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.LocalProtocolParamsRepository;

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
    ProtocolParams protocolParameters = genesisService.findProtocolParametersFromIndexer();
    //then
    assertNotNull(protocolParameters);
  }
}
