package org.cardanofoundation.rosetta.api.config;

import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
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
//        if (ObjectUtils.isEmpty(transactionHashes)) {
//            return;
//        }
        List<String> transactionInformations = transactionHashes.stream()
                .map(transactionHash -> redisTemplate.opsForValue().get(transactionHash)).toList();
        if (transactionHashes.size() != transactionInformations.size()) {
            log.warn("Maybe there are some error");
        }
        Map<String, String> txHashWithData = new HashMap<>();
        for (String transactionHash : transactionHashes) {
            String pendingData = redisTemplate.opsForValue().get(Constants.REDIS_PREFIX_PENDING + transactionHash);
            String txData = Objects.isNull(pendingData) ? redisTemplate.opsForValue().get(Constants.REDIS_PREFIX_MEMPOOL + transactionHash) : pendingData;
            txHashWithData.put(Constants.REDIS_PREFIX_MEMPOOL + transactionHash, txData);
        }
        redisTemplate.delete(redisTemplate.keys(Constants.REDIS_PREFIX_MEMPOOL + "*"));
        txHashWithData.entrySet().stream().forEach((entry) -> {
            log.info("Set redis value {} - {}", entry.getKey(), entry.getValue());
            redisTemplate.opsForValue().set(entry.getKey(), entry.getValue());
        });
        log.info("End the cron job");
    }

    public List<String> getAllTransactionsInMempool() {
        return localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono()
                .blockOptional()
                .orElse(Collections.emptyList()).stream().map(TransactionUtil::getTxHash).toList();
    }
}
