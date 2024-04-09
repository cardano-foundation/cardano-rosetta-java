package org.cardanofoundation.rosetta.testgenerator;

import com.bloxbean.cardano.yaci.test.Funding;
import com.bloxbean.cardano.yaci.test.YaciCardanoContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;
import org.cardanofoundation.rosetta.testgenerator.transactions.TransactionRunner;
import org.reflections.Reflections;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.testcontainers.utility.DockerImageName;

@Component
@Slf4j
@Profile("generator")
public class DataGenerator implements CommandLineRunner {
  public static YaciCardanoContainer cardanoContainer;

  @Override
  public void run(String... args) throws Exception {
    cardanoContainer = new YaciCardanoContainer(DockerImageName.parse("bloxbean/yaci-cli").withTag("0.0.20-beta1"))
        .withInitialFunding(new Funding("addr_test1qp73ljurtknpm5fgey5r2y9aympd33ksgw0f8rc5khheg83y35rncur9mjvs665cg4052985ry9rzzmqend9sqw0cdksxvefah", 1000));
    cardanoContainer.setPortBindings(List.of("3001:3001"));
    cardanoContainer.start();
    startRunner();
  }

  private static void startRunner() {
    Map<String, TransactionBlockDetails> allTransactions = getTransactionRunners()
        .map(transactionRunner -> {
          log.info("Running: {}", transactionRunner.getClass().getSimpleName());
          transactionRunner.init();
          return transactionRunner.runTransactions();
        })
        .flatMap(map -> map.entrySet().stream())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    log.info("All transactions executed successfully");
    writeToJson(allTransactions);
  }

  private static Stream<TransactionRunner> getTransactionRunners() {
    Reflections reflections = new Reflections("org.cardanofoundation.rosetta.testgenerator");
    Set<Class<? extends TransactionRunner>> classes = reflections.getSubTypesOf(
        TransactionRunner.class);
    return classes.stream().map(clazz -> {
      try {
        return clazz.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
               InvocationTargetException e) {
        throw new IllegalArgumentException("Error during creating new instance with reflection", e);
      }
    });
  }

  private static void writeToJson(Map<String, TransactionBlockDetails> generatedDataMap) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    try {
      objectMapper.writeValue(new File(TestConstants.FILE_SAVE_PATH), generatedDataMap);
    } catch (IOException e) {
      throw new MissingResourceException(e.getMessage(), Main.class.getName(),
          TestConstants.FILE_SAVE_PATH);
    }
  }
}
