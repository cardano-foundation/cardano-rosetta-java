package org.cardanofoundation.rosetta.api;

import java.math.BigInteger;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.mockito.Mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.ConfigurationMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ConfigurationMapper.class, SpringMappersTestConfig.class})
public class BaseMapperSetup {

  @MockBean
  protected ProtocolParamService protocolParamService;

  @Mock
  ProtocolParams protocolParams;

  @BeforeEach
  public void before() {
    when(protocolParamService.getProtocolParameters()).thenReturn(protocolParams);
    when(protocolParams.getPoolDeposit()).thenReturn(new BigInteger("500"));
  }
}
