package org.cardanofoundation.rosetta.common.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

@Component
@Slf4j
@RequiredArgsConstructor
public class PostgresNotificationListener {

  private static final String EPOCH_PARAM_NOTIFICATION = "LISTEN epoch_param_new_record";

  private ExecutorService executorService;
  private volatile boolean running = true;

  private final ProtocolParamService protocolParamService;
  private final DataSource dataSource;

  @PostConstruct
  public void init() {
    executorService = Executors.newVirtualThreadPerTaskExecutor();
    executorService.submit(this::listenForNotifications);
  }

  private void listenForNotifications() {
    try (Connection conn = dataSource.getConnection()) {
      PGConnection pgConn = conn.unwrap(PGConnection.class);

      try (Statement stmt = conn.createStatement()) {
        stmt.execute(EPOCH_PARAM_NOTIFICATION);
      }
      while (running) {

        PGNotification[] notifications = pgConn.getNotifications(60000);

        if (notifications != null) {
          for (PGNotification notification : notifications) {
            log.info("A notification was received from the epoch_param table: {}",
                notification.getParameter());
            protocolParamService.updateCachedProtocolParams();
          }
        }
      }
    } catch (Exception e) {
      log.error("[PostgresNotificationListener] An error occurred during listener work: {}",
          e.getMessage());
    }
    try {
      Thread.sleep(6000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void stop() {
    running = false;
  }

  @PreDestroy
  public void destroy() {
    stop();
    if (executorService != null && !executorService.isShutdown()) {
      executorService.shutdown();
    }
  }

}
