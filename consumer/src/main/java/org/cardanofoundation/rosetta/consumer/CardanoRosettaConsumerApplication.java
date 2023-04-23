package org.cardanofoundation.rosetta.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@ComponentScan({"com.sotatek","org.cardanofoundation.rosetta"})
@EnableJpaRepositories({"com.sotatek","org.cardanofoundation.rosetta"})
@EntityScan({"com.sotatek","org.cardanofoundation.rosetta"})
public class CardanoRosettaConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(CardanoRosettaConsumerApplication.class, args);
  }

}
