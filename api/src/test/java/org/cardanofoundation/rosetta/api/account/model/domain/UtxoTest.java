package org.cardanofoundation.rosetta.api.account.model.domain;

import java.util.Collections;
import jakarta.inject.Inject;

import org.modelmapper.ModelMapper;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UtxoTest extends BaseMapperSetup {

  @Inject
  ModelMapper mapper;

  @Test
  void fromUtxoKeyPositiveTest() {
    Utxo utxo = mapper.map(new UtxoKey("txHash", 1), Utxo.class);
    assertEquals("txHash", utxo.getTxHash());
    assertEquals(1, utxo.getOutputIndex());
  }

  @Test
  void fromUtxoKeyNullTest() {
    assertThrows(IllegalArgumentException.class, () -> mapper.map(null, Utxo.class));
  }

  @Test
  void populateFromUtxoEntityPositiveTest() {
    AddressUtxoEntity entity = new AddressUtxoEntity();
    entity.setOwnerAddr("ownerAddr");
    entity.setAmounts(Collections.emptyList());
    Utxo utxo = new Utxo("txHash", 1);

    mapper.map(entity, utxo);

    assertEquals("ownerAddr", utxo.getOwnerAddr());
    assertEquals(Collections.emptyList(), utxo.getAmounts());
  }

  @Test
  void populateFromUtxoEntityNullTest() {
    Utxo utxo = new Utxo("txHash", 1);
    assertThrows(IllegalArgumentException.class, () -> mapper.map(null, utxo));
  }
}
