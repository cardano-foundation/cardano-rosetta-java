package org.cardanofoundation.rosetta.api.config.yaci;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "cardano-transaction-submitter")
public class CardanoTransactionSubmitterProperties {
  Connection connection;
  Long networkMagic;
  Transaction transaction;
  @Getter
  @Setter
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Connection {
    Socket socket;
  }

  @Getter
  @Setter
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Socket {
    String path;
  }

  @Getter
  @Setter
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Transaction {
    Long ttl;
  }

}
