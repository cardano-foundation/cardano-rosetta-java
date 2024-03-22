package org.cardanofoundation.rosetta.testgenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.reflections.Reflections;

import org.cardanofoundation.rosetta.testgenerator.common.GeneratedTestDataDTO;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.transactions.TransactionRunner;

import static org.reflections.scanners.Scanners.SubTypes;

@Slf4j
public class Main {

  public static void main(String[] args)
      throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    GeneratedTestDataDTO generatedTestData = new GeneratedTestDataDTO();

    generatedTestData = runAllFunctions(generatedTestData);
    writeToJson(generatedTestData);

  }

  /**
   * Finding all classes that implement the TransactionRunner interface, initializing them and running the transactions
   * @param generatedTestData - the generatedTestData object where all the data will be stored for the tests
   * @return
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  private static GeneratedTestDataDTO runAllFunctions(GeneratedTestDataDTO generatedTestData)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Reflections reflections = new Reflections("org.cardanofoundation.rosetta.testgenerator");
    Set<Class<?>> classes = reflections.get(SubTypes.of(TransactionRunner.class).asClass());
    for(Class<?> clazz : classes) {
        TransactionRunner transactionGenerator = (TransactionRunner) clazz.getDeclaredConstructor().newInstance();
        log.info("Running: {}", transactionGenerator.getClass().getSimpleName());
        transactionGenerator.init();
        generatedTestData = transactionGenerator.runTransactions(generatedTestData);
    }
    return generatedTestData;
  }

  private static void writeToJson(GeneratedTestDataDTO generatedTestData) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    objectMapper.writeValue(new File(TestConstants.FILE_SAVE_PATH), generatedTestData);
  }

}
