package org.cardanofoundation.rosetta.testgenerator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;

@Slf4j
@SpringBootApplication
@Profile("generator")
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
