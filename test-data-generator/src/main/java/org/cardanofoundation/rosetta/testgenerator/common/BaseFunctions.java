package org.cardanofoundation.rosetta.testgenerator.common;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.exception.ApiRuntimeException;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;

@Slf4j
public class BaseFunctions {

  public static final BackendService backendService = new BFBackendService(
      "http://localhost:8080/api/v1/", "Dummy");
  public static final QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);

  private BaseFunctions() {
  }

  public static void checkIfUtxoAvailable(String txHash, String address) {
    Optional<Utxo> utxo = Optional.empty();
    int count = 0;
    DefaultUtxoSupplier defaultUtxoSupplier = new DefaultUtxoSupplier(
        backendService.getUtxoService());
    while (utxo.isEmpty()) {
      if (count++ >= 20) {
        break;
      }
      List<Utxo> utxos = defaultUtxoSupplier.getAll(address);
      utxo = utxos.stream().filter(u -> u.getTxHash().equals(txHash))
          .findFirst();
      log.info("Try to get new output... txhash: " + txHash);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        log.error("Error in sleep", e);
        Thread.currentThread().interrupt();
      }
    }
  }

  public static double lovelaceToAda(String lovelace) {
    return Integer.parseInt(lovelace) / 1000000.0;
  }

  public static Block getBlock(String txHash) {
    try {
      TransactionContent txContent = backendService.getTransactionService().getTransaction(txHash)
          .getValue();
      Integer blockHeight = txContent.getBlockHeight();
      log.info("Block height: " + blockHeight);
      return backendService.getBlockService()
          .getBlockByNumber(BigInteger.valueOf(blockHeight)).getValue();
    } catch (ApiException e) {
      throw new ApiRuntimeException("Problem during receiving a block", e);
    }
  }
}
