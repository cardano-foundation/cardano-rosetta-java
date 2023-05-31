package org.cardanofoundation.rosetta.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {RosettaApiApplication.class})
@Import(TestConfiguration.class)
public abstract class IntegrationTest {
  @LocalServerPort
  protected int serverPort;

  protected String baseUrl = "http://localhost";

  protected static RestTemplate restTemplate;

  @BeforeAll
  public static void init() {
    restTemplate = new RestTemplate();
  }

  protected ObjectMapper objectMapper = new ObjectMapper();
}
