package org.cardanofoundation.rosetta.yaciindexer;

import java.sql.SQLException;
import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class YaciIndexerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YaciIndexerApplication.class, args);
    }

    @Bean(initMethod = "start", destroyMethod = "stop") //TODO saa extract to @Configuration
    public Server inMemoryH2DatabaseaServer() throws SQLException {
        return Server.createTcpServer(
            "-tcp", "-tcpAllowOthers", "-tcpPort", "9090");
    }

}
