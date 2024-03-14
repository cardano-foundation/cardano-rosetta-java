package org.cardanofoundation.rosetta.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataParseUtil {

  public static Object getObjectFromHashMapObject(Object object, Class classForObject) throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      String s = mapper.writeValueAsString(object);
      return mapper.readValue(s, classForObject);
  }
}
