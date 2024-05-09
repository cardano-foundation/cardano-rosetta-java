package org.cardanofoundation.rosetta.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.rosetta.RosettaApiApplication;

@Profile("test-integration")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {RosettaApiApplication.class})
@Transactional
public abstract class IntegrationTest extends TransactionsTestData {
}
