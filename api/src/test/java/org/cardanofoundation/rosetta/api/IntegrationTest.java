package org.cardanofoundation.rosetta.api;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;

import org.cardanofoundation.rosetta.RosettaApiApplication;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

@Profile("test-integration")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {RosettaApiApplication.class})
@Transactional
public abstract class IntegrationTest {

  protected static Map<String, TransactionBlockDetails> generatedDataMap;

  @Autowired
  public TestRestTemplate restTemplate;

  @LocalServerPort
  protected int serverPort;

  @BeforeAll
  public static void init(@Autowired ObjectMapper objectMapper) throws IOException {
    generatedDataMap = objectMapper.readValue(new File("." + TestConstants.FILE_SAVE_PATH),
        new TypeReference<>() {
        });
  }
}
