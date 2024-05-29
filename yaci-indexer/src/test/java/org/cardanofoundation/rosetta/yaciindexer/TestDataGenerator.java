package org.cardanofoundation.rosetta.yaciindexer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;

import lombok.experimental.UtilityClass;

import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.model.Amount;
import com.bloxbean.cardano.yaci.core.model.AuxData;
import com.bloxbean.cardano.yaci.core.model.BootstrapWitness;
import com.bloxbean.cardano.yaci.core.model.Datum;
import com.bloxbean.cardano.yaci.core.model.Era;
import com.bloxbean.cardano.yaci.core.model.ExUnits;
import com.bloxbean.cardano.yaci.core.model.NativeScript;
import com.bloxbean.cardano.yaci.core.model.PlutusScript;
import com.bloxbean.cardano.yaci.core.model.Redeemer;
import com.bloxbean.cardano.yaci.core.model.RedeemerTag;
import com.bloxbean.cardano.yaci.core.model.Relay;
import com.bloxbean.cardano.yaci.core.model.TransactionBody;
import com.bloxbean.cardano.yaci.core.model.VkeyWitness;
import com.bloxbean.cardano.yaci.core.model.Witnesses;
import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import com.bloxbean.cardano.yaci.core.util.CborSerializationUtil;
import com.bloxbean.cardano.yaci.helper.model.Transaction;
import com.bloxbean.cardano.yaci.helper.model.Utxo;
import com.bloxbean.cardano.yaci.store.blocks.domain.Block;
import com.bloxbean.cardano.yaci.store.blocks.domain.Vrf;
import com.bloxbean.cardano.yaci.store.common.domain.AddressUtxo;
import com.bloxbean.cardano.yaci.store.common.domain.Amt;
import com.bloxbean.cardano.yaci.store.common.domain.TxOuput;
import com.bloxbean.cardano.yaci.store.common.domain.UtxoKey;
import com.bloxbean.cardano.yaci.store.events.EventMetadata;
import com.bloxbean.cardano.yaci.store.events.TransactionEvent;
import com.bloxbean.cardano.yaci.store.staking.domain.Delegation;
import com.bloxbean.cardano.yaci.store.staking.domain.PoolRegistration;
import com.bloxbean.cardano.yaci.store.staking.domain.PoolRetirement;
import com.bloxbean.cardano.yaci.store.staking.domain.StakeRegistrationDetail;
import com.bloxbean.cardano.yaci.store.transaction.domain.TxWitnessType;
import com.bloxbean.cardano.yaci.store.transaction.domain.Txn;
import com.bloxbean.cardano.yaci.store.transaction.domain.TxnWitness;
import com.bloxbean.cardano.yaci.store.transaction.domain.Withdrawal;
import com.fasterxml.jackson.databind.JsonNode;

import static org.mockito.Mockito.mock;

@UtilityClass
public class TestDataGenerator {

  // Custom transaction
  public static TransactionEvent newTransactionEvent() {
    return TransactionEvent.builder()
        .metadata(newEventMetadata())
        .transactions(List.of(newTransaction(1), newTransaction(2), newTransaction(3)))
        .build();
  }

  private static EventMetadata newEventMetadata() {
    return EventMetadata.builder()
        .mainnet(true)
        .protocolMagic(100L)
        .era(Era.Conway)
        .slotLeader("slotLeader")
        .epochNumber(200)
        .block(300L)
        .blockHash("blockHash")
        .blockTime(400L)
        .prevBlockHash("prevBlockHash")
        .slot(500L)
        .epochSlot(600L)
        .noOfTxs(700)
        .syncMode(true)
        .parallelMode(true)
        .remotePublish(true)
        .build();
  }

  private Transaction newTransaction(int i) {
    return Transaction.builder()
        .blockNumber(70000L + i)
        .slot(200L + i)
        .txHash("txHash" + i)
        .body(newTransactionBody(i))
        .utxos(List.of(newUtxo(i), newUtxo(i), newUtxo(i)))
        .collateralReturnUtxo(newUtxo(i))
        .witnesses(newWitnesses(i))
        .auxData(newAuxData(i))
        .invalid(true)
        .build();
  }

  private AuxData newAuxData(int i) {
    return new AuxData(
        newCbor(i),
        newJson(i),
        List.of(newNativeScript(i)),
        List.of(newPlutusScript(i + 1)),
        List.of(newPlutusScript(i + 2)),
        List.of(newPlutusScript(i + 3))
    );
  }

  private static Witnesses newWitnesses(int i) {
    return Witnesses.builder()
        .vkeyWitnesses(List.of(newVkeyWitness(i)))
        .nativeScripts(newNativeScriptList(i))
        .bootstrapWitnesses(List.of(newBootstrapWitnesses(i)))
        .redeemers(List.of(newRedeemer(i)))
        .datums(List.of(newDatum(i)))
        .plutusV1Scripts(List.of(newPlutusScript(i + 1)))
        .plutusV2Scripts(List.of(newPlutusScript(i + 2)))
        .plutusV3Scripts(List.of(newPlutusScript(i + 3)))
        .build();
  }

  private static List<NativeScript> newNativeScriptList(int i) {
    List<NativeScript> list = new ArrayList<>();
    for (int j = 0; j < i; j++) {
      list.add(newNativeScript(i + j));
    }
    return list;
  }

  private static Datum newDatum(int i) {
    return Datum.builder()
        .cbor(newCbor(i))
        .build();
  }

  private static PlutusScript newPlutusScript(int i) {
    return PlutusScript.builder()
        .content(newEncodedHex(i))
        .build();
  }

  private static Redeemer newRedeemer(int i) {
    return Redeemer.builder()
        .tag(RedeemerTag.Cert)
        .index(100)
        .data(newDatum(i))
        .exUnits(newExUnits())
        .cbor(newCbor(i))
        .build();
  }

  private static ExUnits newExUnits() {
    return ExUnits.builder()
        .build();
  }

  private static BootstrapWitness newBootstrapWitnesses(int i) {
    return BootstrapWitness.builder()
        .publicKey(newEncodedHex(1 + i))
        .signature(newEncodedHex(2 + i))
        .chainCode(newEncodedHex(3 + i))
        .attributes(newEncodedHex(4 + i))
        .build();
  }

  private static NativeScript newNativeScript(int i) {
    return NativeScript.builder()
        .content(newJson(i))
        .build();
  }

  private static String newJson(int i) {
    return "{\"type\":\"sig\", \"keyHash\":\"0800fc577294c34e0b28ad283943594" + i + "\"}";
  }

  private static VkeyWitness newVkeyWitness(int i) {
    return VkeyWitness.builder()
        .key(newEncodedHex(i))
        .signature(newEncodedHex(i))
        .build();
  }

  private static String newEncodedHex(int i) {
    byte[] bytes = HexUtil.decodeHexString(
        "5639465ee457792902be570e8659e3a0".repeat(Math.max(0, i)));
    return HexUtil.encodeHexString(bytes);
  }

  private TransactionBody newTransactionBody(int i) {
    return TransactionBody.builder()
        .cbor(newCbor(i))
        .build();
  }

  private Utxo newUtxo(int i) {
    return Utxo.builder()
        .txHash("txHash")
        .index(100 + i)
        .address("address" + i)
        .amounts(List.of(newAmount(10 + i), newAmount(20 + i)))
        .datumHash("datumHash" + i)
        .inlineDatum("inlineDatum" + i)
        .scriptRef("scriptRef" + i)
        .build();
  }

  private static Amount newAmount(int i) {
    return Amount.builder()
        .assetName("assetName" + i)
        .policyId("policyId" + i)
        .quantity(BigInteger.ONE)
        .unit("unit" + i)
        .assetNameBytes(HexFormat.of().parseHex("1234567890"))
        .build();
  }

  private static String newCbor(int i) {
    DataItem[] dataItems = new DataItem[1];
    dataItems[0] = new UnicodeString("unicodeString" + i);
    return HexUtil.encodeHexString(CborSerializationUtil.serialize(dataItems));
  }

  // Mappers
  public static AddressUtxo newAddressUtxo() {
    return AddressUtxo.builder()
        .txHash("txHash")
        .outputIndex(100)
        .slot(200L)
        .blockHash("blockHash")
        .epoch(300)
        .ownerAddr("ownerAddr")
        .ownerStakeAddr("ownerStakeAddr")
        .ownerPaymentCredential("ownerPaymentCredential")
        .ownerStakeCredential("ownerStakeCredential")
        .lovelaceAmount(BigInteger.ONE)
        .amounts(List.of(new Amt()))
        .dataHash("dataHash")
        .inlineDatum("inlineDatum")
        .scriptRef("scriptRef")
        .referenceScriptHash("referenceScriptHash")
        .isCollateralReturn(true)
        .build();
  }

  public static Block newBlock() {
    return Block.builder()
        .hash("hash")
        .number(100L)
        .slot(200L)
        .totalOutput(BigInteger.ONE)
        .totalFees(BigInteger.TWO)
        .blockTime(300L)
        .epochNumber(400)
        .epochSlot(500)
        .era(600)
        .prevHash("prevHash")
        .issuerVkey("issuerVkey")
        .vrfVkey("vrfVkey")
        .nonceVrf(Vrf.builder().output("output1").proof("proof1").build())
        .leaderVrf(Vrf.builder().output("output2").proof("proof2").build())
        .vrfResult(Vrf.builder().output("output3").proof("proof3").build())
        .opCertHotVKey("opCertHotVKey")
        .opCertSeqNumber(700)
        .opcertKesPeriod(800)
        .opCertSigma("opCertSigma")
        .blockBodySize(900L)
        .blockBodyHash("blockBodyHash")
        .protocolVersion("protocolVersion")
        .noOfTxs(1000)
        .slotLeader("slotLeader")
        .build();
  }

  public static PoolRegistration newPoolRegistration() {
    return PoolRegistration.builder()
        .txHash("txHash")
        .certIndex(100)
        .poolId("poolId")
        .vrfKeyHash("vrfKeyHash")
        .pledge(BigInteger.ONE)
        .cost(BigInteger.TWO)
        .margin(200.0)
        .rewardAccount("rewardAccount")
        .poolOwners(new HashSet<>(List.of("poolOwners")))
        .relays(Collections.singletonList(Relay.builder()
            .port(500)
            .ipv4("ipv4")
            .ipv6("ipv6")
            .dnsName("dnsName")
            .build())
        )
        .metadataUrl("metadataUrl")
        .metadataHash("metadataHash")
        .epoch(300)
        .slot(400L)
        .blockHash("blockHash")
        .build();
  }

  public static PoolRetirement newPoolRetirement() {
    return PoolRetirement.builder()
        .txHash("txHash")
        .certIndex(100)
        .poolId("poolId")
        .retirementEpoch(200)
        .epoch(300)
        .slot(400L)
        .blockHash("blockHash")
        .build();
  }

  public static StakeRegistrationDetail newStakeRegistrationDetail() {
    return StakeRegistrationDetail.builder()
        .credential("credential")
        .address("address")
        .txHash("txHash")
        .certIndex(100)
        .type(CertificateType.STAKE_REGISTRATION)
        .epoch(200)
        .slot(300L)
        .blockHash("blockHash")
        .blockNumber(400L)
        .blockTime(500L)
        .build();
  }

  public static Delegation newDelegation() {
    return Delegation.builder()
        .credential("credential")
        .address("address")
        .poolId("poolId")
        .txHash("txHash")
        .certIndex(100)
        .epoch(200)
        .slot(300L)
        .blockHash("blockHash")
        .blockNumber(400L)
        .blockTime(500L)
        .build();
  }

  public static Txn newTxn() {
    return Txn.builder()
        .txHash("txHash")
        .blockHash("blockHash")
        .slot(100L)
        .inputs(Collections.singletonList(
            UtxoKey.builder()
                .txHash("txHash1")
                .outputIndex(1000)
                .build()
        ))
        .outputs(Collections.singletonList(
            UtxoKey.builder()
                .txHash("txHash2")
                .outputIndex(2000)
                .build()
        ))
        .fee(BigInteger.TWO)
        .ttl(200L)
        .auxiliaryDataHash("auxiliaryDataHash")
        .validityIntervalStart(300L)
        .scriptDataHash("scriptDataHash")
        .collateralInputs(Collections.singletonList(
            UtxoKey.builder()
                .txHash("txHash3")
                .outputIndex(3000)
                .build()
        ))
        .requiredSigners(new HashSet<>(List.of("requiredSigners")))
        .netowrkId(300)
        .collateralReturn(UtxoKey.builder()
            .txHash("txHash4")
            .outputIndex(5000)
            .build()
        )
        .collateralReturnJson(TxOuput.builder()
            .address("address")
            .amounts(Collections.singletonList(Amt.builder()
                .unit("unit")
                .policyId("policyId")
                .assetName("assetName")
                .quantity(BigInteger.TEN)
                .build()
            )).build())
        .totalCollateral(BigInteger.TWO)
        .referenceInputs(Collections.singletonList(
            UtxoKey.builder()
                .txHash("txHash5")
                .outputIndex(5000)
                .build()
        ))
        .invalid(true)
        .blockNumber(400L)
        .blockTime(500L)
        .build();
  }

  public static TxnWitness newTxnWitness() {
    return TxnWitness.builder()
        .txHash("txHash")
        .index(100)
        .pubKey("pubKey")
        .signature("signature")
        .pubKeyhash("pubKeyhash")
        .type(TxWitnessType.BOOTSTRAP_WITNESS)
        .additionalData(mock(JsonNode.class))
        .slot(200L)
        .build();
  }

  public static Withdrawal newWithdrawal() {
    return Withdrawal.builder()
        .address("address")
        .txHash("txHash")
        .amount(BigInteger.ONE)
        .epoch(100)
        .slot(200L)
        .blockNumber(400L)
        .blockTime(500L)
        .build();
  }


}
