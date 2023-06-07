package org.cardanofoundation.rosetta.api;

import java.io.File;
import java.io.IOException;
import org.cardanofoundation.rosetta.api.constructionApiService.impl.IntegrationTest;
import org.testcontainers.containers.DockerComposeContainer;

public class IntegrationTestWithDB extends IntegrationTest {
  public static final DockerComposeContainer<?> testEnvironment;

  static {
    try {
      // Get absolute path for docker-compose file
      File fileWithAbsolutePath =
          new File(
              "../api/src/test/resources/testcontainers/docker-compose.yml").getCanonicalFile();
      testEnvironment =
          new DockerComposeContainer(fileWithAbsolutePath);
      testEnvironment.start();

      Thread.sleep(10000); // pause for 1 minutes
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot initialize testcontainer!", e);
    } catch (InterruptedException e) {
      // handle the exception if the thread is interrupted while sleeping
      throw new RuntimeException("Cannot sleep Thread!", e);

    }
  }
}
