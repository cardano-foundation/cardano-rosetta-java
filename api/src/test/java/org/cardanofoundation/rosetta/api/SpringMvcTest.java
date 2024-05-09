package org.cardanofoundation.rosetta.api;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;

import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

@SpringBootTest
@AutoConfigureMockMvc
public class SpringMvcTest {

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected MockMvc mockMvc;

  protected static Map<String, TransactionBlockDetails> generatedDataMap;


  @Deprecated
  protected int serverPort;

  @BeforeAll
  public static void init() throws IOException {
    generatedDataMap = new ObjectMapper().readValue(
        new File("." + TestConstants.FILE_SAVE_PATH),
        new TypeReference<>() {
        });
  }

}
