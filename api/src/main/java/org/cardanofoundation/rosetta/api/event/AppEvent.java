package org.cardanofoundation.rosetta.api.event;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Component
public class AppEvent {
  @Autowired
  private Dotenv dotenv;

  public static String networkId;
  public static BigInteger networkMagic;
  public static String cardanoNodePath;
  @EventListener(ApplicationReadyEvent.class)
  public  void afterStartApp() throws FileNotFoundException, ParseException {
    File genesisFile;
    String genesisPath = dotenv.get("GENESIS_SHELLEY_PATH");
    genesisFile = ResourceUtils.getFile(genesisPath);
    InputStream input = new FileInputStream(genesisFile);
    HashMap<String,Object> object = (HashMap<String,Object>) new JSONParser(input).parse();
    networkId = ((String) object.get("networkId")).toLowerCase();
    networkMagic = (BigInteger) object.get("networkMagic");
    cardanoNodePath = dotenv.get("CARDANO_NODE_PATH");
  }
}
