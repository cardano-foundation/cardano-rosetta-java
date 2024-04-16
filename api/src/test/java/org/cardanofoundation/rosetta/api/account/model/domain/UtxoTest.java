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
    entity.setOwnerAddr("ownerAddr");
    entity.setAmounts(Collections.emptyList());
    Utxo utxo = new Utxo("txHash", 1);

    utxo.populateFromUtxoEntity(entity);

    assertEquals("ownerAddr", utxo.getOwnerAddr());
    assertEquals(Collections.emptyList(), utxo.getAmounts());
  }

  @Test
  void populateFromUtxoEntityNullTest() {
    Utxo utxo = new Utxo("txHash", 1);
    assertThrows(NullPointerException.class, () -> utxo.populateFromUtxoEntity(null));
  }
}
