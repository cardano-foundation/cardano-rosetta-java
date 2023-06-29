package org.cardanofoundation.rosetta.consumer.service.impl.genesis;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.consumer.service.GenesisFetching;
import org.cardanofoundation.rosetta.consumer.util.FileUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;


@Slf4j
@Component
@Profile("!internet")
public class GenesisLocalFetching implements GenesisFetching {

  /**
   * get json string from input url
   *
   * @param url
   * @return json String
   */
  @Override
  public String getContent(String url) {
    try {
      ClassLoader cl = ClassUtils.getDefaultClassLoader();
      ResourceUtils.getFile(url).getAbsolutePath();
      System.out.println("abc");
      System.out.println("url is " + ResourceUtils.getFile(url).getAbsolutePath().toString());
      return FileUtil.readFile(ResourceUtils.getFile(url).getAbsolutePath());
    } catch (FileNotFoundException e) {
      log.error("exception {} with url {}", e.getMessage(), url);
      throw new IllegalStateException("can't load file " + url);
    }
  }
}

