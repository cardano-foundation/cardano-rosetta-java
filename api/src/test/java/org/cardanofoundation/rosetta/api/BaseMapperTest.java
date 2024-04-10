package org.cardanofoundation.rosetta.api;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.ConfigurationMapper;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ConfigurationMapper.class, SpringMappersTestConfig.class})
public class BaseMapperTest {
  @MockBean
  protected ProtocolParamService protocolParamService;
}

