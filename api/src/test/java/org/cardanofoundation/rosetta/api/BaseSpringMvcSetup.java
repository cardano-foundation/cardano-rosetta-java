package org.cardanofoundation.rosetta.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
public class BaseSpringMvcSetup extends TransactionsTestData {

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected MockMvc mockMvc;

}
