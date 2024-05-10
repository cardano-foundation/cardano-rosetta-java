package org.cardanofoundation.rosetta.yaciindexer;

import java.sql.SQLException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Configuration
@ConditionalOnClass(org.h2.tools.Server.class)
public class ConfigurationH2 {

  private static final Logger log = LoggerFactory.getLogger(ConfigurationH2.class);
  @Bean(initMethod = "start", destroyMethod = "stop")
  public Server inMemoryH2DatabaseaServer() throws SQLException {
    int port = 9090;
    log.debug("Starting H2 database server on tcp port: {}", port);
    return Server.createTcpServer(
        "-tcp", "-tcpAllowOthers", "-tcpPort", String.valueOf(port));
  }
}
