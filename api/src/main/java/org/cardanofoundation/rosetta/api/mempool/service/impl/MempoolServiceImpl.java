package org.cardanofoundation.rosetta.api.mempool.service.impl;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.rosetta.api.mempool.service.MempoolService;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.RawTransaction;
import org.openapitools.client.model.TransactionIdentifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "cardano.rosetta.MEMPOOL_ENABLED", havingValue = "true")
public class MempoolServiceImpl implements MempoolService {

  private final NetworkService networkService;
  private LocalClientProvider localClientProvider;
  private LocalTxMonitorClient txMonitorClient;
  @Value("${cardano.rosetta.API_NODE_SOCKET_PATH}")
  private String nodeSocketPath;
  @Value("${cardano.rosetta.DEVKIT_ENABLED}")
  private boolean devkitEnabled;
  @Value("${cardano.rosetta.DEVKIT_PORT}")
  private int devkitPort;
  @Value("${cardano.rosetta.DEVKIT_URL}")
  private String devkitHost;
  List<RawTransaction> rawTransactions = new ArrayList<>();

  /**
   * Fetches the mempool transactions from the node and stores them in the rawTransactions list.
   * This method is scheduled to run always with one invocation at a time.
   */
  @Scheduled(fixedDelay = 1)
  private void fetchMemPool() {
    log.info("Start fetching mempool transactions");
    if (!localClientProvider.isRunning()) {
      localClientProvider.start();
    }
    rawTransactions = txMonitorClient.acquireAndGetMempoolTransactions().map(bytes -> {
      log.info("TxHash: {}", TransactionUtil.getTxHash(bytes));
      String txHash = TransactionUtil.getTxHash(bytes);
      return new RawTransaction(txHash, bytes);
    }).collectList().block();
  }

  @PostConstruct
  public void init() {
    log.info("MempoolServiceImpl initialized");
    localClientProvider = createLocalClientProvider();
    localClientProvider.start();
    txMonitorClient = localClientProvider.getTxMonitorClient();
  }

  @Override
  public List<TransactionIdentifier> getCurrentTransactionIdentifiers(String network) {
    List<TransactionIdentifier> list = rawTransactions.stream()
        .map(rawTransaction ->
            TransactionIdentifier
            .builder()
            .hash(rawTransaction.txhash())
            .build()).toList();
    return list;
  }

  private LocalClientProvider createLocalClientProvider() {
    Network supportedNetwork = networkService.getSupportedNetwork();
    if (devkitEnabled) {
      return new LocalClientProvider(devkitHost, devkitPort, supportedNetwork.getProtocolMagic());
    } else {
      return new LocalClientProvider(nodeSocketPath, supportedNetwork.getProtocolMagic());
    }
  }
}
