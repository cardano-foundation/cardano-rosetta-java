package org.cardanofoundation.rosetta.yaciindexer.stores;

import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.SimpleValue;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
import com.bloxbean.cardano.yaci.helper.model.Transaction;
import com.bloxbean.cardano.yaci.store.events.TransactionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

      // adding txBody to signedTransaction
      DataItem txBody = CborSerializationUtil.deserialize(HexUtil.decodeHexString(tx.getBody().getCbor()))[0];
      signedTransaction.put(new UnsignedInteger(0), txBody);

      // adding witnesses to signedTransaction
      Map witnessSet = new Map();
      addvKeyWitness(tx, witnessSet);
      addNativeScripts(tx, witnessSet);
      addBootstrapWitness(tx, witnessSet);
      // todo add scripts and redeemers

      if(!witnessSet.getKeys().isEmpty()) {
        signedTransaction.put(new UnsignedInteger(1), witnessSet);
      }
      signedTransaction.put(new UnsignedInteger(2), SimpleValue.TRUE);
      addAuxData(tx, signedTransaction);
      byte[] serialize = CborSerializationUtil.serialize(signedTransaction);


      int txSize = serialize.length;
      // Save the transaction
      TransactionSizeEntity transactionSizeEntity = TransactionSizeEntity.builder()
          .txHash(tx.getTxHash())
          .size(txSize)
          .blockNumber(tx.getBlockNumber())
          .build();
      transactionSizeEntities.add(transactionSizeEntity);
    });

    if(!transactionSizeEntities.isEmpty()) {
      transactionSizeRepository.saveAll(transactionSizeEntities);
    }
  }

  private static void addBootstrapWitness(Transaction tx, Map witnessSet) {
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
    if(tx.getAuxData() != null) {
      Array auxiliaryData = new Array();
      auxiliaryData.add(CborSerializationUtil.deserialize(HexUtil.decodeHexString(tx.getAuxData().getMetadataCbor()))[0]);
      signedTransaction.put(new UnsignedInteger(3), auxiliaryData);
    }
  }

  private static void addvKeyWitness(Transaction tx, Map witnessSet) {
    if(!tx.getWitnesses().getVkeyWitnesses().isEmpty()) {
      Array vKeyWitnessArray = new Array();
      tx.getWitnesses().getVkeyWitnesses().forEach(vkeyWitness -> {
        Array vitnessArray = new Array();
        vitnessArray.add(new ByteString(HexUtil.decodeHexString(vkeyWitness.getKey())));
        vitnessArray.add(new ByteString(HexUtil.decodeHexString(vkeyWitness.getSignature())));
        vKeyWitnessArray.add(vitnessArray);
      });
      witnessSet.put(new UnsignedInteger(0), vKeyWitnessArray);
    }
  }

  private static void addNativeScripts(Transaction tx, Map witnessSet) {
    if(!tx.getWitnesses().getNativeScripts().isEmpty()) {
      Array nativeScripts = new Array();
      tx.getWitnesses().getNativeScripts().forEach(script -> {
        nativeScripts.add(new ByteString(HexUtil.decodeHexString(
            "45d70e54f3b5e9c5a2b0cd417028197bd6f5fa5378c2f5eba896678d"))); // TODO : Need to get the script hash
      });
      witnessSet.put(new UnsignedInteger(1), nativeScripts);
    }
  }

}
