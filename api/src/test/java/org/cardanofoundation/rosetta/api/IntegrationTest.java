package org.cardanofoundation.rosetta.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.rosetta.RosettaApiApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Profile("test-integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
    RosettaApiApplication.class})
@Import(TestConfiguration.class)
public abstract class IntegrationTest {

  protected static RestTemplate restTemplate;

  @LocalServerPort
  protected int serverPort;
  protected String baseUrl = "http://localhost";
  protected ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  public static void init() {
    restTemplate = new RestTemplate();
  }

  @AfterAll
  public static void teadDown() {
  }
}
