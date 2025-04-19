package org.cardanofoundation.rosetta.testgenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.reflections.Reflections;

import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;
import org.cardanofoundation.rosetta.testgenerator.transactions.TransactionRunner;

@Slf4j
public class TestDataGeneratorApp {

  public static void main(String[] args) {
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
      throw new MissingResourceException(e.getMessage(), TestDataGeneratorApp.class.getName(),
          TestConstants.FILE_SAVE_PATH);
    }
  }
}
