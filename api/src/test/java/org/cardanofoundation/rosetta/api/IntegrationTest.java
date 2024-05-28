package org.cardanofoundation.rosetta.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

import org.cardanofoundation.rosetta.RosettaApiApplication;

@Profile("test-integration")
@SpringBootTest(classes = {RosettaApiApplication.class})
public abstract class IntegrationTest extends TransactionsTestData {
}
