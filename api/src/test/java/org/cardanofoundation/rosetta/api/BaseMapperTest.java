package org.cardanofoundation.rosetta.api;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.ConfigurationMapper;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ConfigurationMapper.class, SpringMappersTestConfig.class})
public class BaseMapperTest {

}

