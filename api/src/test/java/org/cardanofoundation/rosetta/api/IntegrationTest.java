package org.cardanofoundation.rosetta.api;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;

import org.cardanofoundation.rosetta.RosettaApiApplication;
import org.cardanofoundation.rosetta.testgenerator.common.GeneratedTestDataDTO;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;

@Profile("test-integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
    RosettaApiApplication.class})
@Transactional
public abstract class IntegrationTest {

  protected static GeneratedTestDataDTO generatedTestData;

  @Autowired
  public TestRestTemplate restTemplate;

  @LocalServerPort
  protected int serverPort;

  @BeforeAll
  public static void init(@Autowired ObjectMapper objectMapper) throws IOException {
    generatedTestData = objectMapper.readValue(new File("." + TestConstants.FILE_SAVE_PATH),
        GeneratedTestDataDTO.class);
  }
}
