package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.rosetta.api.RosettaApiApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = RosettaApiApplication.class)
public abstract class IntegrationTest {
  @LocalServerPort
  protected int serverPort;

  protected String baseUrl = "http://localhost";

  protected static RestTemplate restTemplate;

  protected ObjectMapper objectMapper = new ObjectMapper();
}
