package org.cardanofoundation.rosetta.common.ledgersync.mapper;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeCredential;
import java.io.IOException;

public class StakeCredentialDeserializer extends KeyDeserializer {


  @Override
  public Object deserializeKey(String s, DeserializationContext deserializationContext)
      throws IOException {
    return new ObjectMapper().readValue(s, StakeCredential.class);
  }
}
