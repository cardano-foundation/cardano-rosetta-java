package org.cardanofoundation.rosetta.consumer.unit.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.Datum;
import org.cardanofoundation.rosetta.common.ledgersync.Witnesses;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxOut;
import org.cardanofoundation.rosetta.consumer.repository.DatumRepository;
import org.cardanofoundation.rosetta.consumer.service.impl.DatumServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DatumServiceImplTest {

  @Test
  void handleDatum() {
    DatumRepository datumRepository = Mockito.mock(DatumRepository.class);
    AggregatedTx aggregatedTx = Mockito.mock(AggregatedTx.class);
    Witnesses transactionWitness = Mockito.mock(Witnesses.class);
    Datum datumWitness = Mockito.mock(Datum.class);
    String txHash = "6497b33b10fa2619c6efbd9f874ecd1c91badb10bf70850732aab45b90524d9e";
    Tx tx = Mockito.mock(Tx.class);
    Map<String, Tx> txMap = Map.of(txHash, tx);
    AggregatedTxOut aggregatedTxOut = Mockito.mock(AggregatedTxOut.class);
    AggregatedTxOut aggregatedTxOut2 = Mockito.mock(AggregatedTxOut.class);
    Collection<AggregatedTx> aggregatedTxs = List.of(aggregatedTx);
    List<AggregatedTxOut> txOutputs = Arrays.asList(aggregatedTxOut, aggregatedTxOut2);

    Mockito.when(datumWitness.getHash()).thenReturn("81c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d");
    Mockito.when(datumWitness.getCbor()).thenReturn("d81858a5d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff");
    Mockito.when(datumWitness.getJson()).thenReturn("{\"bytes\":\"d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff\"}");

    List<Datum> datumsWitness = List.of(datumWitness);

    Datum inlineDatum = Mockito.mock(Datum.class);
   Datum inlineDatum2 = Mockito.mock(Datum.class);

    Mockito.when(aggregatedTx.getHash()).thenReturn(txHash);
    Mockito.when(aggregatedTx.getWitnesses()).thenReturn(transactionWitness);
    Mockito.when(transactionWitness.getDatums()).thenReturn(datumsWitness);

    Mockito.when(aggregatedTx.getTxOutputs()).thenReturn(txOutputs);
    Mockito.when(tx.getValidContract()).thenReturn(true);

    Mockito.when(aggregatedTxOut.getInlineDatum()).thenReturn(inlineDatum);
    Mockito.when(aggregatedTxOut2.getInlineDatum()).thenReturn(inlineDatum2);
    Mockito.when(inlineDatum.getHash()).thenReturn("81c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d");
    Mockito.when(inlineDatum.getCbor()).thenReturn("d81858a5d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff");
    Mockito.when(inlineDatum.getJson()).thenReturn("{\"bytes\":\"d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff\"}");

    Mockito.when(inlineDatum2.getHash()).thenReturn("71c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d");
    Mockito.when(inlineDatum2.getCbor()).thenReturn("a81858a5d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff");
    Mockito.when(inlineDatum2.getJson()).thenReturn("{\"bytes\":\"a8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff\"}");

    DatumServiceImpl laclase = new DatumServiceImpl(datumRepository);

    laclase.handleDatum(aggregatedTxs, txMap);
    Mockito.verify(datumRepository, Mockito.times(1)).getExistHashByHashIn(Mockito.anySet());
    Mockito.verify(datumRepository, Mockito.times(1)).saveAll(Mockito.anyCollection());
  }

  @Test
  void handleDatumWitnessOutputExist() {
    DatumRepository datumRepository = Mockito.mock(DatumRepository.class);
    AggregatedTx aggregatedTx = Mockito.mock(AggregatedTx.class);
    Witnesses transactionWitness = Mockito.mock(Witnesses.class);
    Datum datumWitness = Mockito.mock(Datum.class);
    String txHash = "6497b33b10fa2619c6efbd9f874ecd1c91badb10bf70850732aab45b90524d9e";
    Tx tx = Mockito.mock(Tx.class);
    Map<String, Tx> txMap = Map.of(txHash, tx);
    AggregatedTxOut aggregatedTxOut = Mockito.mock(AggregatedTxOut.class);
    AggregatedTxOut aggregatedTxOut2 = Mockito.mock(AggregatedTxOut.class);
    Collection<AggregatedTx> aggregatedTxs = List.of(aggregatedTx);
    List<AggregatedTxOut> txOutputs = Arrays.asList(aggregatedTxOut, aggregatedTxOut2);

    Mockito.when(datumWitness.getHash()).thenReturn("81c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d");
    Mockito.when(datumWitness.getCbor()).thenReturn("d81858a5d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff");
    Mockito.when(datumWitness.getJson()).thenReturn("{\"bytes\":\"d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff\"}");

    List<Datum> datumsWitness = List.of(datumWitness);

    Datum inlineDatum = Mockito.mock(Datum.class);
    Datum inlineDatum2 = Mockito.mock(Datum.class);

    Mockito.when(aggregatedTx.getHash()).thenReturn(txHash);
    Mockito.when(aggregatedTx.getWitnesses()).thenReturn(transactionWitness);
    Mockito.when(transactionWitness.getDatums()).thenReturn(datumsWitness);

    Mockito.when(aggregatedTx.getTxOutputs()).thenReturn(txOutputs);
    Mockito.when(tx.getValidContract()).thenReturn(true);

    Mockito.when(aggregatedTxOut.getInlineDatum()).thenReturn(inlineDatum);
    Mockito.when(aggregatedTxOut2.getInlineDatum()).thenReturn(inlineDatum2);
    Mockito.when(inlineDatum.getHash()).thenReturn("81c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d");
    Mockito.when(inlineDatum.getCbor()).thenReturn("d81858a5d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff");
    Mockito.when(inlineDatum.getJson()).thenReturn("{\"bytes\":\"d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff\"}");

    Mockito.when(inlineDatum2.getHash()).thenReturn("71c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d");
    Mockito.when(inlineDatum2.getCbor()).thenReturn("a81858a5d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff");
    Mockito.when(inlineDatum2.getJson()).thenReturn("{\"bytes\":\"a8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff\"}");

    Mockito.when(datumRepository.getExistHashByHashIn(Mockito.anySet())).thenReturn(Collections.singleton("81c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d"));
    DatumServiceImpl laclase = new DatumServiceImpl(datumRepository);

    laclase.handleDatum(aggregatedTxs, txMap);
    Mockito.verify(datumRepository, Mockito.times(1)).getExistHashByHashIn(Mockito.anySet());
    Mockito.verify(datumRepository, Mockito.times(1)).saveAll(Mockito.anyCollection());
  }

  @Test
  void handleDatumWitnessOutputNull() {
    DatumRepository datumRepository = Mockito.mock(DatumRepository.class);
    AggregatedTx aggregatedTx = Mockito.mock(AggregatedTx.class);
    Witnesses transactionWitness = Mockito.mock(Witnesses.class);
    Datum datumWitness = Mockito.mock(Datum.class);
    String txHash = "6497b33b10fa2619c6efbd9f874ecd1c91badb10bf70850732aab45b90524d9e";
    Tx tx = Mockito.mock(Tx.class);
    Map<String, Tx> txMap = Map.of(txHash, tx);
    AggregatedTxOut aggregatedTxOut = Mockito.mock(AggregatedTxOut.class);
    AggregatedTxOut aggregatedTxOut2 = Mockito.mock(AggregatedTxOut.class);
    Collection<AggregatedTx> aggregatedTxs = List.of(aggregatedTx);
    List<AggregatedTxOut> txOutputs = Arrays.asList(aggregatedTxOut, aggregatedTxOut2);

    Mockito.when(datumWitness.getHash()).thenReturn("81c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d");
    Mockito.when(datumWitness.getCbor()).thenReturn("d81858a5d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff");
    Mockito.when(datumWitness.getJson()).thenReturn("{\"bytes\":\"d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff\"}");

    Datum inlineDatum = Mockito.mock(Datum.class);
    Datum inlineDatum2 = Mockito.mock(Datum.class);

    Mockito.when(aggregatedTx.getHash()).thenReturn(txHash);
    Mockito.when(aggregatedTx.getWitnesses()).thenReturn(transactionWitness);
    Mockito.when(aggregatedTx.getTxOutputs()).thenReturn(txOutputs);
    Mockito.when(tx.getValidContract()).thenReturn(true);

    Mockito.when(aggregatedTxOut.getInlineDatum()).thenReturn(null);
    Mockito.when(aggregatedTxOut2.getInlineDatum()).thenReturn(null);
    Mockito.when(inlineDatum.getHash()).thenReturn("81c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d");
    Mockito.when(inlineDatum.getCbor()).thenReturn("d81858a5d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff");
    Mockito.when(inlineDatum.getJson()).thenReturn("{\"bytes\":\"d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff\"}");

    Mockito.when(inlineDatum2.getHash()).thenReturn("71c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d");
    Mockito.when(inlineDatum2.getCbor()).thenReturn("a81858a5d8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff");
    Mockito.when(inlineDatum2.getJson()).thenReturn("{\"bytes\":\"a8799fd8799fd8799fd8799f582069a4199509a6bc81daf91eea261f14b8e67870fa501accbad154cd8857d5a257ff02ff581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd227447617364666173644b6661736466736164666173d87a9fffd8799fffd8799fd8799f581c0a0297ac3c9004d38307c8601351df65392952dc0f1ee66694dd2274ffd87a9fffff9fff200000486173646661736466ffff\"}");

    Mockito.when(datumRepository.getExistHashByHashIn(Mockito.anySet())).thenReturn(Collections.singleton("81c4b709d63f814af964013721d35aa0f4c91e75de8274db47dfd5a4b377eb7d"));
    DatumServiceImpl laclase = new DatumServiceImpl(datumRepository);

    laclase.handleDatum(aggregatedTxs, txMap);
    Mockito.verify(datumRepository, Mockito.times(0)).getExistHashByHashIn(Mockito.anySet());
    Mockito.verify(datumRepository, Mockito.times(1)).saveAll(Mockito.anyCollection());
  }

}