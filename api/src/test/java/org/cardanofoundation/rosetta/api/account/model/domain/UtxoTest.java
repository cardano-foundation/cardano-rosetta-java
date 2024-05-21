package org.cardanofoundation.rosetta.api.account.model.domain;

import java.util.Collections;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.cardanofoundation.rosetta.api.account.mapper.AddressUtxoEntityToUtxo;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.block.mapper.UtxoKeyToEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtxoTest extends BaseMapperSetup {

  @Inject
  UtxoKeyToEntity mapper;
  @Inject
  AddressUtxoEntityToUtxo addressUtxoEntityToUtxo;

  @Test
  void fromUtxoKeyPositiveTest() {
    Utxo utxo = mapper.fromEntity(new UtxoKey("txHash", 1));
    assertEquals("txHash", utxo.getTxHash());
    assertEquals(1, utxo.getOutputIndex());
  }

  @Test
  void populateFromUtxoEntityPositiveTest() {
    AddressUtxoEntity entity = new AddressUtxoEntity(null, null, "ownerAddr",
        Collections.emptyList());

    Utxo utxo = addressUtxoEntityToUtxo.toDto(entity);

    assertEquals("ownerAddr", utxo.getOwnerAddr());
    assertEquals(Collections.emptyList(), utxo.getAmounts());
  }

}
