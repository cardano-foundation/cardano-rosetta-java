package org.cardanofoundation.rosetta.yaciindexer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ConfigurationH2.class)
public class YaciIndexerApplication {

  public static void main(String[] args) {
    SpringApplication.run(YaciIndexerApplication.class, args);
  }

}
