package org.cardanofoundation.rosetta.yaciindexer.stores.txsize;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bloxbean.cardano.yaci.core.model.Witnesses;
import com.bloxbean.cardano.yaci.helper.model.Transaction;
import com.bloxbean.cardano.yaci.store.events.TransactionEvent;

import org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model.TransactionSizeEntity;
import org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model.TransactionSizeRepository;

import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomTransactionSizeStore {

  private final TransactionSizeRepository transactionSizeRepository;

  @EventListener
  @Transactional
  public void handleTransactionEvent(TransactionEvent transactionEvent) {
    try {
      List<TransactionSizeEntity> transactionSizeEntities = transactionEvent.getTransactions()
              .stream()
              .map(CustomTransactionSizeStore::convertToEntity)
              .filter(Objects::nonNull)
              .toList();

      transactionSizeRepository.saveAll(transactionSizeEntities);
    } catch (Exception e) {
      // DO NOT STOP PROCESSING IF ERROR OCCURRED
      log.error("Error while saving transaction size entities", e);

      // TODO add storing error to db?
    }
  }

  /**
   * Size from the original tx bytes (YaciConfig.returnFullTxCbor), so it matches the ledger / db-sync tx.size.
   */
  @Nullable
  private static TransactionSizeEntity convertToEntity(Transaction tx) {
    Integer size = tx.getTxCbor() == null ? null : TxSizeUtil.ledgerSize(decodeHexString(tx.getTxCbor()));
    if (size == null) {
      log.warn("No full CBOR for transaction {} in block {}, skipping its size", tx.getTxHash(), tx.getBlockNumber());
      return null;
    }

    return TransactionSizeEntity.builder()
            .txHash(tx.getTxHash())
            .size(size)
            .scriptSize(plutusScriptSize(tx.getWitnesses()))
            .blockNumber(tx.getBlockNumber())
            .build();
  }

  private static int plutusScriptSize(Witnesses witnesses) {
    return Stream.of(witnesses.getPlutusV1Scripts(), witnesses.getPlutusV2Scripts(), witnesses.getPlutusV3Scripts())
            .flatMap(List::stream)
            .mapToInt(script -> script.getContent().length() / 2)
            .sum();
  }

}
