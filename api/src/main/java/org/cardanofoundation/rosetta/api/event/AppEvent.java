package org.cardanofoundation.rosetta.api.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Component
public class AppEvent {
  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisPath;

  public static String networkId;
  public static BigInteger networkMagic;
  @EventListener(ApplicationReadyEvent.class)
  public void afterStartApp() throws IOException, ParseException, URISyntaxException {
    File file = ResourceUtils.getFile("classpath:" + genesisPath);
    InputStream input = new FileInputStream(file);
    HashMap<String,Object> object = (HashMap<String,Object>) new JSONParser(input).parse();
    networkId = ((String) object.get("networkId")).toLowerCase();
    networkMagic = (BigInteger) object.get("networkMagic");
  }
}
