package org.cardanofoundation.rosetta.api.account.model.domain;

import java.math.BigInteger;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtxoTest {

  @Test
  void fromUtxoKeyPositiveTest() {
    Utxo utxo = Utxo.fromUtxoKey(new UtxoKey("txHash", 1));
    assertEquals("txHash", utxo.getTxHash());
    assertEquals(1, utxo.getOutputIndex());
  }

  @Test
  void fromUtxoKeyNullTest() {
    assertThrows(NullPointerException.class, () -> Utxo.fromUtxoKey(null));
  }

  @Test
  void populateFromUtxoEntityPositiveTest() {
    AddressUtxoEntity entity = new AddressUtxoEntity();
    entity.setSlot(1L);
    entity.setBlockHash("blockHash");
    entity.setEpoch(1);
    entity.setOwnerAddr("ownerAddr");
    entity.setOwnerAddrFull("ownerAddrFull");
    entity.setOwnerStakeAddr("ownerStakeAddr");
    entity.setOwnerPaymentCredential("ownerPaymentCredential");
    entity.setOwnerStakeCredential("ownerStakeCredential");
    entity.setLovelaceAmount(BigInteger.ONE);
    entity.setAmounts(Collections.emptyList());
    entity.setDataHash("dataHash");
    entity.setInlineDatum("inlineDatum");
    entity.setScriptRef("scriptRef");
    entity.setReferenceScriptHash("referenceScriptHash");
    entity.setIsCollateralReturn(true);
    Utxo utxo = new Utxo("txHash", 1);

    utxo.populateFromUtxoEntity(entity);

    assertEquals(1L, utxo.getSlot());
    assertEquals("blockHash", utxo.getBlockHash());
    assertEquals(1, utxo.getEpoch());
    assertEquals("ownerAddr", utxo.getOwnerAddr());
    assertEquals("ownerAddrFull", utxo.getOwnerAddrFull());
    assertEquals("ownerStakeAddr", utxo.getOwnerStakeAddr());
    assertEquals("ownerPaymentCredential", utxo.getOwnerPaymentCredential());
    assertEquals("ownerStakeCredential", utxo.getOwnerStakeCredential());
    assertEquals(Collections.emptyList(), utxo.getAmounts());
    assertEquals(BigInteger.ONE, utxo.getLovelaceAmount());
    assertEquals("dataHash", utxo.getDataHash());
    assertEquals("inlineDatum", utxo.getInlineDatum());
    assertEquals("scriptRef", utxo.getScriptRef());
    assertEquals("referenceScriptHash", utxo.getReferenceScriptHash());
    assertTrue(utxo.getIsCollateralReturn());
  }

  @Test
  void populateFromUtxoEntityNullTest() {
    Utxo utxo = new Utxo("txHash", 1);
    assertThrows(NullPointerException.class, () -> utxo.populateFromUtxoEntity(null));
  }
}
