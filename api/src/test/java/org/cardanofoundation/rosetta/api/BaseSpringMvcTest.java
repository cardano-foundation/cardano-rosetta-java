package org.cardanofoundation.rosetta.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
<<<<<<<< HEAD:api/src/test/java/org/cardanofoundation/rosetta/api/SpringMvcSetup.java
public class SpringMvcSetup {
========
public class BaseSpringMvcTest extends TransactionsTestData {
>>>>>>>> refs/heads/main:api/src/test/java/org/cardanofoundation/rosetta/api/BaseSpringMvcTest.java

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected MockMvc mockMvc;

}
