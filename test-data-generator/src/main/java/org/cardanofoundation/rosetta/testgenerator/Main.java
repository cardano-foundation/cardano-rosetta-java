package org.cardanofoundation.rosetta.testgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.GeneratedTestDataDTO;

public class Main {

  public static void main(String[] args) throws IOException {
    GeneratedTestDataDTO generatedTestData = new GeneratedTestDataDTO();
    SimpleTransactions simpleTransactions = new SimpleTransactions();
    generatedTestData = simpleTransactions.runFunctions(generatedTestData);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    objectMapper.writeValue(new File(TestConstants.FILE_SAVE_PATH), generatedTestData);

  }

}