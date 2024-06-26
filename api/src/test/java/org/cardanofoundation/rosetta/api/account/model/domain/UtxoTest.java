package org.cardanofoundation.rosetta.api.account.model.domain;

import java.util.Collections;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.cardanofoundation.rosetta.api.account.mapper.AddressUtxoEntityToUtxo;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtxoTest extends BaseMapperSetup {

  @Inject
  BlockMapper mapper;
  @Inject
  AddressUtxoEntityToUtxo addressUtxoEntityToUtxo;

  @Test
  void fromUtxoKeyPositiveTest() {
    Utxo utxo = mapper.getUtxoFromUtxoKey(new UtxoKey("txHash", 1));
    assertEquals("txHash", utxo.getTxHash());
    assertEquals(1, utxo.getOutputIndex());
  }

  @Test
  void populateFromUtxoEntityPositiveTest() {
    AddressUtxoEntity entity = new AddressUtxoEntity(null, null, "ownerAddr", "ownerStakeAddr",
        Collections.emptyList(), 0L);

    Utxo utxo = addressUtxoEntityToUtxo.toDto(entity);

    assertEquals("ownerAddr", utxo.getOwnerAddr());
    assertEquals(Collections.emptyList(), utxo.getAmounts());
  }

}
