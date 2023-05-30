package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.cardanofoundation.rosetta.api.RosettaApiApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
    RosettaApiApplication.class})
public abstract class IntegrationTest {

  public static final DockerComposeContainer<?> testEnvironment;
  protected static RestTemplate restTemplate;

  static {
    try {
      // Get absolute path for docker-compose file
      File fileWithAbsolutePath =
          new File(
              "../api/src/test/resources/testcontainers/docker-compose.yml").getCanonicalFile();
      testEnvironment =
          new DockerComposeContainer(fileWithAbsolutePath);
      testEnvironment.start();

      Thread.sleep(60000); // pause for 2 minutes
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot initialize testcontainer!", e);
    } catch (InterruptedException e) {
      // handle the exception if the thread is interrupted while sleeping
      throw new RuntimeException("Cannot sleep Thread!", e);

    }
  }

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
