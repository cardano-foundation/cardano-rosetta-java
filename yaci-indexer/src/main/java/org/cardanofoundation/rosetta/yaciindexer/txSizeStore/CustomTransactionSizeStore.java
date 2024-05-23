package org.cardanofoundation.rosetta.yaciindexer.txSizeStore;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.SimpleValue;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.script.NativeScript;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.model.PlutusScript;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
import com.bloxbean.cardano.yaci.helper.model.Transaction;
import com.bloxbean.cardano.yaci.store.events.TransactionEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.yaciindexer.txSizeStore.model.TransactionSizeEntity;
import org.cardanofoundation.rosetta.yaciindexer.txSizeStore.model.TransactionSizeRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomTransactionSizeStore {

  private final TransactionSizeRepository transactionSizeRepository;

  @EventListener
  @Transactional
  public void handleTransactionEvent(TransactionEvent transactionEvent) {
    List<TransactionSizeEntity> transactionSizeEntities = new ArrayList<>();
    transactionEvent.getTransactions().forEach(tx -> {

      // Reconstructing Transaction based on CDDL specification - https://github.com/IntersectMBO/cardano-ledger/blob/master/eras/alonzo/impl/cddl-files/alonzo.cddl
      Map signedTransaction = new Map();

      DataItem txBody = CborSerializationUtil.deserialize(HexUtil.decodeHexString(tx.getBody().getCbor()))[0];
      signedTransaction.put(new UnsignedInteger(TransactionBuildingConstants.TX_BODY_INDEX), txBody);
      int scriptSize = addWitnessSetToSignedTransaction(tx, signedTransaction);

      if(tx.getBlockNumber() > TransactionBuildingConstants.ALONZO_START_BLOCKNUMBER) { // starting from alonzo era
        signedTransaction.put(new UnsignedInteger(
            TransactionBuildingConstants.SUCCESS_INDICATOR_INDEX), SimpleValue.TRUE);
      }

      addAuxDataToTransaction(tx, signedTransaction);


      byte[] serialize = CborSerializationUtil.serialize(signedTransaction);


      int txSize = serialize.length;
      // Save the transaction
      TransactionSizeEntity transactionSizeEntity = TransactionSizeEntity.builder()
          .txHash(tx.getTxHash())
          .size(txSize)
          .scriptSize(scriptSize)
          .blockNumber(tx.getBlockNumber())
          .build();
      transactionSizeEntities.add(transactionSizeEntity);
    });

    if(!transactionSizeEntities.isEmpty()) {
      transactionSizeRepository.saveAll(transactionSizeEntities);
    }
  }

  /**
   * Reconstructing the Witness_set from transaction and adding it to signedTransaction
   * CDDL Definition: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L269
   * @param tx
   * @param signedTransaction
   * @return
   */
  private static int addWitnessSetToSignedTransaction(Transaction tx, Map signedTransaction) {
    // adding witnesses to signedTransaction
    Map witnessSet = new Map();
    int scriptSize = 0;
    addvKeyWitnessToWitness(tx, witnessSet);
    addNativeScriptsToWitness(tx, witnessSet);
    addBootstrapToWitness(tx, witnessSet);

    scriptSize += addPlutusToWitness(witnessSet, tx.getWitnesses().getPlutusV1Scripts(),
        TransactionBuildingConstants.PLUTUSV1_WITNESS_INDEX);
    scriptSize += addPlutusToWitness(witnessSet, tx.getWitnesses().getPlutusV2Scripts(), TransactionBuildingConstants.PLUTUSV2_WITNESS_INDEX);

    addDatumToWitness(tx, witnessSet);
    addRedeemerToWitness(tx, witnessSet);

    if(!witnessSet.getKeys().isEmpty()) {
      signedTransaction.put(new UnsignedInteger(TransactionBuildingConstants.WITNESS_SET_INDEX), witnessSet);
    }
    return scriptSize;
  }

  /**
   * Adding the datum to Witness set. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L280
   * @param tx transaction to extract the datum
   * @param witnessSet witnessSet to add the datum
   */
  private static void addDatumToWitness(Transaction tx, Map witnessSet) {
    if(!tx.getWitnesses().getDatums().isEmpty()) {
      Array array = new Array();
      tx.getWitnesses().getDatums().forEach(datum -> {
        array.add(new ByteString(HexUtil.decodeHexString(datum.getCbor()))); // could speed it up by passing an empty array, since we are only interested in the size not the content
      });
      witnessSet.put(new UnsignedInteger(TransactionBuildingConstants.PLUTUS_DATUM_WITNESS_INDEX), array);
    }
  }

  /**
   * Adding Plutus Script data to witnessSet. Can be used for V1, V2 and V3. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L278
   * @param witnessSet witnessset to add the data
   * @param scripts List of PlutusScripts
   * @param witnessSetIndex Index where to add the datum based on cddl spec
   * @return
   */
  private static int addPlutusToWitness(Map witnessSet, List<PlutusScript> scripts, int witnessSetIndex) {
    AtomicInteger scriptSize = new AtomicInteger();
    Array array = new Array();
    if(!scripts.isEmpty()) {
      scripts.forEach(script -> {
        scriptSize.addAndGet(script.getContent().length() / 2); // adding have the string length, sinze it's 4bit hex and we need the byte length
        array.add(new ByteString(HexUtil.decodeHexString(script.getContent())));
      });
      witnessSet.put(new UnsignedInteger(witnessSetIndex), array);
    }
    return scriptSize.get();
  }

  /**
   * Adding Redemer data to witnessset. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L302
   * @param tx transaction to extract the redeemer data
   * @param witnessSet witnesset to add the data based on cddl spec
   */
  private static void addRedeemerToWitness(Transaction tx, Map witnessSet) {
    if(!tx.getWitnesses().getRedeemers().isEmpty()) {
      Array array = new Array();
      tx.getWitnesses().getRedeemers().forEach(redeemer -> {
        array.add(new ByteString(HexUtil.decodeHexString(redeemer.getCbor()))); // could speed it up by passing an empty array, since we are only interested in the size not the content
      });
      witnessSet.put(new UnsignedInteger(TransactionBuildingConstants.REDEEMER_WITNESS_INDEX), array);
    }
  }

  /**
   * Extracting bootstrap data and adding it to witnessSet. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L348
   * @param tx Transcation to get the bootstrap from
   * @param witnessSet witnessSet to add the data
   */
  private static void addBootstrapToWitness(Transaction tx, Map witnessSet) {
    if(!tx.getWitnesses().getBootstrapWitnesses().isEmpty()) {
      Array array = new Array();
      tx.getWitnesses().getBootstrapWitnesses().forEach(bootstrapWitness -> {
        Array witnessArray = new Array();
        witnessArray.add(new ByteString(HexUtil.decodeHexString(bootstrapWitness.getPublicKey())));
        witnessArray.add(new ByteString(HexUtil.decodeHexString(bootstrapWitness.getSignature())));
        witnessArray.add(new ByteString(HexUtil.decodeHexString(bootstrapWitness.getChainCode())));
        witnessArray.add(new ByteString(HexUtil.decodeHexString(bootstrapWitness.getAttributes())));
        array.add(witnessArray);
      });
      witnessSet.put(new UnsignedInteger(TransactionBuildingConstants.BOOTSTRAP_WITNESS_INDEX), array);
    }
  }

  /**
   * Extracting the Auxiliary data and adding it to the transaction. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L17
   * @param tx Transcation to get the auxdata from
   * @param signedTransaction Map to add the auxdata
   */
  private static void addAuxDataToTransaction(Transaction tx, Map signedTransaction) {
    if(tx.getAuxData() != null)  {
      Array auxiliaryData = new Array();
      if(tx.getAuxData().getMetadataCbor() != null) {
        auxiliaryData.add(CborSerializationUtil.deserialize(
            HexUtil.decodeHexString(tx.getAuxData().getMetadataCbor()))[0]);
      }
      signedTransaction.put(new UnsignedInteger(TransactionBuildingConstants.AUXILIARY_DATA_INDEX), auxiliaryData);
    }
  }

  /**
   * Extracting VKey and adding it to witnessSet. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L346
   * @param tx transaction to extract the data
   * @param witnessSet witnessSet to add the data
   */
  private static void addvKeyWitnessToWitness(Transaction tx, Map witnessSet) {
    if(!tx.getWitnesses().getVkeyWitnesses().isEmpty()) {
      Array vKeyWitnessArray = new Array();
      tx.getWitnesses().getVkeyWitnesses().forEach(vkeyWitness -> {
        Array vitnessArray = new Array();
        vitnessArray.add(new ByteString(HexUtil.decodeHexString(vkeyWitness.getKey())));
        vitnessArray.add(new ByteString(HexUtil.decodeHexString(vkeyWitness.getSignature()))); // could speed it up by passing an empty array, since we are only interested in the size not the content
        vKeyWitnessArray.add(vitnessArray);
      });
      witnessSet.put(new UnsignedInteger(TransactionBuildingConstants.VKEY_WITNESS_INDEX), vKeyWitnessArray);
    }
  }

  /**
   * Extracting NativeScript data and adding it to witnessSet. CDDL spec: https://github.com/IntersectMBO/cardano-ledger/blob/e6b6d4f85fb72b5cb5b5361e534d3bb71bb9e55e/eras/alonzo/impl/cddl-files/alonzo.cddl#L355
   * @param tx transaction to extract the data
   * @param witnessSet witnessSet to add the data
   */
  private static void addNativeScriptsToWitness(Transaction tx, Map witnessSet) {
    if(!tx.getWitnesses().getNativeScripts().isEmpty()) {
      Array nativeScripts = new Array();
      tx.getWitnesses().getNativeScripts().forEach(script -> {
            NativeScript nativeScript;
            try {
              nativeScript = NativeScript.deserializeJson(script.getContent());
              nativeScripts.add(new ByteString(nativeScript.getScriptHash()));
            } catch (CborDeserializationException | JsonProcessingException |
                     CborSerializationException e) {
              throw new RuntimeException(e);
            }
      });
      witnessSet.put(new UnsignedInteger(TransactionBuildingConstants.NATIVESCRIPT_WITNESS_INDEX), nativeScripts);
    }
  }

}
