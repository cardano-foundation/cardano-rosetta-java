package org.cardanofoundation.rosetta.api;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;

import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

@SpringBootTest
abstract class TransactionsTestData {

  protected static Map<String, TransactionBlockDetails> generatedDataMap;

  @BeforeAll
  public static void init(@Autowired ObjectMapper objectMapper) throws IOException {
    generatedDataMap = objectMapper.readValue(
        new File("." + TestConstants.FILE_SAVE_PATH),
        new TypeReference<>() {});
  }
}
