package org.cardanofoundation.rosetta.consumer.unit.service.impl;

import org.cardanofoundation.rosetta.consumer.service.impl.GenesisDataServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GenesisServiceTest {
  @InjectMocks
  GenesisDataServiceImpl genesisDataService;
}
