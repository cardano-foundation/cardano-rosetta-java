package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.rosetta.crawler.RosettaApiApplication;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = RosettaApiApplication.class)
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
