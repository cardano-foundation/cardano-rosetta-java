package org.cardanofoundation.rosetta.yaciindexer.stores;

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
import org.cardanofoundation.rosetta.yaciindexer.model.entity.TransactionSizeEntity;
import org.cardanofoundation.rosetta.yaciindexer.model.repository.TransactionSizeRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomTxStorage {

  private final TransactionSizeRepository transactionSizeRepository;

  @EventListener
  @Transactional
  public void handleTransactionEvent(TransactionEvent transactionEvent) {
    List<TransactionSizeEntity> transactionSizeEntities = new ArrayList<>();
    transactionEvent.getTransactions().forEach(tx -> {
      Map signedTransaction = new Map();

//      DataItem txBody = CborSerializationUtil.deserialize(HexUtil.decodeHexString(tx.getBody().getCbor()))[0];
//      signedTransaction.put(new UnsignedInteger(0), txBody);

      int scriptSize = addWitnessSetToSignedTransaction(tx, signedTransaction);

      if(tx.getBlockNumber() > 64902L) { // starting from alonzo era?
        signedTransaction.put(new UnsignedInteger(2), SimpleValue.TRUE);
      }

      addAuxData(tx, signedTransaction);


      byte[] serialize = CborSerializationUtil.serialize(signedTransaction);


      int txSize = serialize.length + tx.getBody().getCbor().length() / 2; // using half the length of cbor string, since it's hex and a hex is 4 bits and a char is 8bits. So dividing by 2 results in the byte size.
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

  private static int addWitnessSetToSignedTransaction(Transaction tx, Map signedTransaction) {
    // adding witnesses to signedTransaction
    Map witnessSet = new Map();
    int scriptSize = 0;
    addvKeyWitnessToWitness(tx, witnessSet);
    addNativeScriptsToWitness(tx, witnessSet);
    addBootstrapToWitness(tx, witnessSet);

    scriptSize += addPlutusToWitness(tx, witnessSet, tx.getWitnesses().getPlutusV1Scripts(), 3);
    scriptSize += addPlutusToWitness(tx, witnessSet, tx.getWitnesses().getPlutusV2Scripts(), 6);
    scriptSize += addPlutusToWitness(tx, witnessSet, tx.getWitnesses().getPlutusV3Scripts(), 7);

    addDatumToWitness(tx, witnessSet);
    addRedeemerToWitness(tx, witnessSet);

    if(!witnessSet.getKeys().isEmpty()) {
      signedTransaction.put(new UnsignedInteger(1), witnessSet);
    }
    return scriptSize;
  }

  private static void addDatumToWitness(Transaction tx, Map witnessSet) {
    if(!tx.getWitnesses().getDatums().isEmpty()) {
      Array array = new Array();
      tx.getWitnesses().getDatums().forEach(datum -> {
        array.add(new ByteString(HexUtil.decodeHexString(datum.getCbor()))); // TODO could speed it up by passing an empty array, since we are only interested in the size not the content
      });
      witnessSet.put(new UnsignedInteger(4), array);
    }
  }

  private static int addPlutusToWitness(Transaction tx, Map witnessSet, List<PlutusScript> scripts, int witnessSetIndex) {
    AtomicInteger scriptSize = new AtomicInteger();
    Array array = new Array();
    if(!scripts.isEmpty()) {
      scripts.forEach(script -> {
        scriptSize.addAndGet(script.getContent().length() / 2);
        array.add(new ByteString(HexUtil.decodeHexString(script.getContent())));
      });
      witnessSet.put(new UnsignedInteger(witnessSetIndex), array);
    }
    return scriptSize.get();
  }


  private static void addRedeemerToWitness(Transaction tx, Map witnessSet) {
    if(!tx.getWitnesses().getRedeemers().isEmpty()) {
      Array array = new Array();
      tx.getWitnesses().getRedeemers().forEach(redeemer -> {
        array.add(new ByteString(HexUtil.decodeHexString(redeemer.getCbor()))); // TODO could speed it up by passing an empty array, since we are only interested in the size not the content
      });
      witnessSet.put(new UnsignedInteger(5), array);
    }
  }

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
      witnessSet.put(new UnsignedInteger(2), array);
    }
  }

  private static void addAuxData(Transaction tx, Map signedTransaction) {
    if(tx.getAuxData() != null)  {
      Array auxiliaryData = new Array();
      if(tx.getAuxData().getMetadataCbor() != null) {
        auxiliaryData.add(CborSerializationUtil.deserialize(
            HexUtil.decodeHexString(tx.getAuxData().getMetadataCbor()))[0]);
      }
      if(tx.getAuxData().getNativeScripts() != null) {
        tx.getAuxData().getNativeScripts().forEach(script -> {
          try {
            auxiliaryData.add(new ByteString(NativeScript.deserializeJson(script.getContent()).getScriptHash()));
          } catch (CborDeserializationException | JsonProcessingException | CborSerializationException e) {
            throw new RuntimeException(e);
          }
        });
      }
      if(tx.getAuxData().getPlutusV1Scripts() != null) {
        tx.getAuxData().getPlutusV1Scripts().forEach(script -> {
          try {
            auxiliaryData.add(new ByteString(NativeScript.deserializeJson(script.getContent()).getScriptHash()));
          } catch (CborDeserializationException | JsonProcessingException | CborSerializationException e) {
            throw new RuntimeException(e);
          }
        });
      }
      if(tx.getAuxData().getPlutusV2Scripts() != null) {
        tx.getAuxData().getPlutusV2Scripts().forEach(script -> {
          try {
            auxiliaryData.add(new ByteString(NativeScript.deserializeJson(script.getContent()).getScriptHash()));
          } catch (CborDeserializationException | JsonProcessingException | CborSerializationException e) {
            throw new RuntimeException(e);
          }
        });
      }
      if(tx.getAuxData().getPlutusV3Scripts() != null) {
        tx.getAuxData().getPlutusV3Scripts().forEach(script -> {
          try {
            auxiliaryData.add(new ByteString(NativeScript.deserializeJson(script.getContent()).getScriptHash()));
          } catch (CborDeserializationException | JsonProcessingException | CborSerializationException e) {
            throw new RuntimeException(e);
          }
        });
      }
      signedTransaction.put(new UnsignedInteger(3), auxiliaryData);
    }
  }

  private static void addvKeyWitnessToWitness(Transaction tx, Map witnessSet) {
    if(!tx.getWitnesses().getVkeyWitnesses().isEmpty()) {
      Array vKeyWitnessArray = new Array();
      tx.getWitnesses().getVkeyWitnesses().forEach(vkeyWitness -> {
        Array vitnessArray = new Array();
        vitnessArray.add(new ByteString(HexUtil.decodeHexString(vkeyWitness.getKey())));
        vitnessArray.add(new ByteString(HexUtil.decodeHexString(vkeyWitness.getSignature()))); // TODO could speed it up by passing an empty array, since we are only interested in the size not the content
        vKeyWitnessArray.add(vitnessArray);
      });
      witnessSet.put(new UnsignedInteger(0), vKeyWitnessArray);
    }
  }

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
      witnessSet.put(new UnsignedInteger(1), nativeScripts);
    }
  }

}
