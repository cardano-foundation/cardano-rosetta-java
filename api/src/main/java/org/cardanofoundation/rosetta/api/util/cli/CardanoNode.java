package org.cardanofoundation.rosetta.api.util.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CardanoNode {
  public static String getCardanoNodeVersion(String cardanoNodePath)
      throws IOException, InterruptedException {
    log.info("[getCardanoNodeVersion] Invoking cardano-node --version at " + cardanoNodePath);
    Process process = new ProcessBuilder(cardanoNodePath , "--version").start();
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new RuntimeException("Failed to execute cardano-node: " + exitCode);
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      return reader.readLine();
    }

  }
}
