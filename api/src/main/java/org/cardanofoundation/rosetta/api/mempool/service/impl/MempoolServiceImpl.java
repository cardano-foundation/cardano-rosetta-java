package org.cardanofoundation.rosetta.api.mempool.service.impl;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.mempool.service.MempoolService;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.mapper.CborMapToTransactionExtraData;
import org.cardanofoundation.rosetta.common.mapper.CborTransactionToRosettaTransaction;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.RawTransaction;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.openapitools.client.model.TransactionIdentifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
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
  boolean connected = false;

  Runnable txMonitorThread = () -> {
    while(true) {
      if (!localClientProvider.isRunning()) {
        localClientProvider.reconnect();
      }
      connected = localClientProvider.isRunning();
      log.info("Waiting to acquire next Snapshot");
      List<byte[]> txBytesList = txMonitorClient.acquireAndGetMempoolTransactionsAsMono().block();

      rawTransactions.clear();
      assert txBytesList != null;
      for(byte[] txBytes: txBytesList) {
        String txHash = TransactionUtil.getTxHash(txBytes);
        log.info("TxHash: {}", txHash);

        // conversion part
        Transaction tx;
        try {
          tx = Transaction.deserialize(txBytes);
        } catch (CborDeserializationException e) {
          throw new RuntimeException(e);
        }

        TransactionBody body = tx.getBody();
        TransactionExtraData extraData;
        Array deserialize = (Array) CborSerializationUtil.deserialize(txBytes);

        extraData = CborMapToTransactionExtraData.convertCborMapToTransactionExtraData(
            (Map) deserialize.getDataItems().get(0));

        TransactionData transactionData = new TransactionData(body,extraData);

        rawTransactions.add(new RawTransaction(txHash, txBytes));



      }
    }
  };

  private Thread thread;
  @PostConstruct
  public void init() {
    log.info("MempoolServiceImpl initialized");
    localClientProvider = createLocalClientProvider();
    localClientProvider.start();
    txMonitorClient = localClientProvider.getTxMonitorClient();
    thread = new Thread(txMonitorThread, "TxMonitorThread");
    thread.start();
  }


  @Override
  public List<TransactionIdentifier> getCurrentTransactionIdentifiers(String network) {
    log.info("Connected: {}", connected);
    List<TransactionIdentifier> list = rawTransactions.stream()
        .map(rawTransaction ->
            TransactionIdentifier
            .builder()
            .hash(rawTransaction.txhash())
            .build()).toList();
    return list;
  }

  @Override
  public org.openapitools.client.model.Transaction getMempoolTransaction(String hash) {
    Optional<RawTransaction> first = rawTransactions.stream().filter(tx -> tx.txhash().equals(hash))
        .findFirst();
    return first.map(CborTransactionToRosettaTransaction::convert).orElse(null);
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
