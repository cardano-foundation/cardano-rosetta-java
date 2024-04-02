package org.cardanofoundation.rosetta.api;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import org.junit.jupiter.api.BeforeEach;

public class BaseMapperTest {

  protected ModelMapper modelMapper;

  @BeforeEach
  void setUp() {
    modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
  }
}
