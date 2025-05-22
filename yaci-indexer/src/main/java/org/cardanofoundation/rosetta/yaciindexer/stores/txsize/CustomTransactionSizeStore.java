package org.cardanofoundation.rosetta.yaciindexer.stores.txsize;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
import com.bloxbean.cardano.yaci.helper.model.Transaction;
import com.bloxbean.cardano.yaci.store.events.TransactionEvent;

import org.cardanofoundation.rosetta.yaciindexer.domain.model.TransactionBuildingConstants;
import org.cardanofoundation.rosetta.yaciindexer.service.TransactionScriptSizeCalculator;
import org.cardanofoundation.rosetta.yaciindexer.service.TransactionSizeCalculator;
import org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model.TransactionSizeEntity;
import org.cardanofoundation.rosetta.yaciindexer.stores.txsize.model.TransactionSizeRepository;

import static co.nstant.in.cbor.model.SimpleValue.TRUE;
import static org.cardanofoundation.rosetta.yaciindexer.domain.model.TransactionBuildingConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomTransactionSizeStore {

  private final TransactionSizeRepository transactionSizeRepository;
  private final TransactionSizeCalculator transactionSizeCalculator;
  private final TransactionScriptSizeCalculator transactionScriptSizeCalculator;

  @EventListener
  @Transactional
  public void handleTransactionEvent(TransactionEvent transactionEvent) {
    try {
      List<TransactionSizeEntity> transactionSizeEntities = transactionEvent.getTransactions()
              .stream()
              .map(this::convertToEntity)
              .toList();

      transactionSizeRepository.saveAll(transactionSizeEntities);
    } catch (Exception e) {
      // DO NOT STOP PROCESSING IF ERROR OCCURRED
      log.error("Error while saving transaction size entities", e);

      // TODO add storing error to db?
    }
  }

  private TransactionSizeEntity convertToEntity(Transaction tx) {
    Map signedTransaction = new Map();

    DataItem txBody = CborSerializationUtil.deserialize(HexUtil.decodeHexString(tx.getBody().getCbor()))[0];
    signedTransaction.put(new UnsignedInteger(TX_BODY_INDEX), txBody);

    // TODO this is specific for main-net but not other environments, right??
    if (tx.getBlockNumber() >= ALONZO_START_BLOCKNUMBER) { // starting from alonzo era
      signedTransaction.put(new UnsignedInteger(SUCCESS_INDICATOR_INDEX), TRUE);
    }

    addAuxDataToTransaction(tx, signedTransaction);

    return TransactionSizeEntity.builder()
            .txHash(tx.getTxHash())
            .size(transactionSizeCalculator.calculateSize(signedTransaction))
            .scriptSize(transactionScriptSizeCalculator.calculateScriptSize(tx, signedTransaction))
            .blockNumber(tx.getBlockNumber())
            .build();
  }

  /**
   * Extracting the Auxiliary data and adding it to the transaction. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L17
   *
   * @param tx Transcation to get the auxdata from
   * @param signedTransaction Map to add the auxdata
   */
  private static void addAuxDataToTransaction(Transaction tx, Map signedTransaction) {
    Optional.ofNullable(tx.getAuxData()).ifPresent(auxData -> {
      Array auxiliaryData = new Array();

      Optional.ofNullable(tx.getAuxData().getMetadataCbor()).ifPresent(cbor -> {

        auxiliaryData.add(CborSerializationUtil.deserialize(
                HexUtil.decodeHexString(tx.getAuxData().getMetadataCbor()))[0]);
        signedTransaction.put(new UnsignedInteger(TransactionBuildingConstants.AUXILIARY_DATA_INDEX), auxiliaryData);
      });
    });
  }

}
