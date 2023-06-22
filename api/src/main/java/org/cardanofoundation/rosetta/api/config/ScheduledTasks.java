package org.cardanofoundation.rosetta.api.config;

import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
@Slf4j
public class ScheduledTasks {

  private final LocalTxMonitorClient localTxMonitorClient;
  private final RedisTemplate<String, String> redisTemplate;

  public ScheduledTasks(LocalTxMonitorClient localTxMonitorClient,
      @Qualifier("redisTemplateString") RedisTemplate<String, String> redisTemplate) {
    this.localTxMonitorClient = localTxMonitorClient;
    this.redisTemplate = redisTemplate;
  }

  @Scheduled(fixedDelayString = "${scheduler.time:1}")
  public void scheduleTaskWithFixedRate() {
    log.info("Start the cron Job ");
    List<String> transactionHashes = getAllTransactionsInMempool();
    log.info("There are {} transactions in mempool", transactionHashes.size());
    if (ObjectUtils.isEmpty(transactionHashes)) {
      return;
    }
    List<Object> txResults = redisTemplate.execute(new SessionCallback<List<Object>>() {
      public List<Object> execute(RedisOperations operations) throws DataAccessException {
        operations.multi();
        List<String> transactionInformations = transactionHashes.stream()
            .map(transactionHash -> redisTemplate.opsForValue().get(transactionHash)).toList();
        if (transactionHashes.size() != transactionInformations.size()) {
          log.warn("Maybe there are some error");
        }
        Objects.requireNonNull(redisTemplate.getConnectionFactory())
            .getConnection()
            .serverCommands()
            .flushAll();
        IntStream.range(0, transactionInformations.size())
            .forEach(index -> redisTemplate.opsForValue()
                .set(transactionHashes.get(index), transactionInformations.get(index)));

        // This will contain the results of all operations in the transaction
        return operations.exec();
      }
    });
    log.info("Number of items added to redis {}", txResults.get(0));
  }

  public List<String> getAllTransactionsInMempool() {
    return localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono()
        .blockOptional()
        .orElse(Collections.emptyList()).stream().map(TransactionUtil::getTxHash).toList();
  }
}
