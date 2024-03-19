package org.cardanofoundation.rosetta.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.cardanofoundation.rosetta.RosettaApiApplication;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.GeneratedTestDataDTO;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Profile("test-integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
    RosettaApiApplication.class})
@Transactional
public abstract class IntegrationTest {

  protected static RestTemplate restTemplate;
  protected static GeneratedTestDataDTO generatedTestData;

  @LocalServerPort
  protected int serverPort;

  @BeforeAll
  public static void init() throws IOException {
    restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();
    generatedTestData = objectMapper.readValue(new File("." + TestConstants.FILE_SAVE_PATH), GeneratedTestDataDTO.class);
  }
}
