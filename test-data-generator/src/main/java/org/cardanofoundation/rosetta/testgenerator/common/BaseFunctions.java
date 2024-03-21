package org.cardanofoundation.rosetta.testgenerator.common;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.cardanofoundation.rosetta.testgenerator.transactions.impl.SimpleTransactions;

public class BaseFunctions {


  public static BackendService backendService = new BFBackendService("http://localhost:8080/api/v1/", "Dummy");
  public static QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);

  public static void checkIfUtxoAvailable(String txHash, String address) {
    Optional<Utxo> utxo = Optional.empty();
    int count = 0;
    DefaultUtxoSupplier defaultUtxoSupplier = new DefaultUtxoSupplier(backendService.getUtxoService());
    while (!utxo.isPresent()) {
      if (count++ >= 20)
        break;
      List<Utxo> utxos = defaultUtxoSupplier.getAll(address);
      utxo = utxos.stream().filter(u -> u.getTxHash().equals(txHash))
          .findFirst();
      System.out.println("Try to get new output... txhash: " + txHash);
      try {
        Thread.sleep(1000);
      } catch (Exception e) {
      }
    }
  }

  public static int lovelaceToAda(String lovelace) {
    return Integer.parseInt(lovelace) / 1000000;
  }

  public static Block getBlock(String txHash) {
    try {
      TransactionContent txContent = backendService.getTransactionService().getTransaction(txHash)
          .getValue();
      Integer blockHeight = txContent.getBlockHeight();
      System.out.println("Block height: " + blockHeight);
      Block block = backendService.getBlockService()
          .getBlockByNumber(BigInteger.valueOf(blockHeight)).getValue();
      return block;
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }
}
