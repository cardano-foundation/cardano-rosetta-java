package org.cardanofoundation.rosetta.consumer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@Profile("test-integration")
public class ExplorerConsumerApplicationTest {

}
