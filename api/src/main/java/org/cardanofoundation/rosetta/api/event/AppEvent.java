package org.cardanofoundation.rosetta.api.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Component
@RequiredArgsConstructor
public class AppEvent {
  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisPath;

  private final ResourceLoader resourceLoader;

  public static String networkId;
  public static BigInteger networkMagic;
  @EventListener(ApplicationReadyEvent.class)
  public void afterStartApp() throws IOException, ParseException {

//    File file = ResourceUtils.getFile("classpath:" + genesisPath);
    Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:" + genesisPath);
    InputStream input = resources[0].getInputStream();
    HashMap<String,Object> object = (HashMap<String,Object>) new JSONParser(input).parse();
    networkId = ((String) object.get("networkId")).toLowerCase();
    networkMagic = (BigInteger) object.get("networkMagic");
  }
}
