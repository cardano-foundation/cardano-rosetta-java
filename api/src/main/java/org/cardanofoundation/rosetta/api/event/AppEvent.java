package org.cardanofoundation.rosetta.api.event;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppEvent {
  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisPath;

  public static String networkId;
  public static BigInteger networkMagic;
  @EventListener(ApplicationReadyEvent.class)
  public void afterStartApp() throws FileNotFoundException, ParseException {
    InputStream input = new FileInputStream(genesisPath);
    HashMap<String,Object> object = (HashMap<String,Object>) new JSONParser(input).parse();
    networkId = ((String) object.get("networkId")).toLowerCase();
    networkMagic = (BigInteger) object.get("networkMagic");
  }
}
