package org.cardanofoundation.rosetta.api.account.model.domain;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressBalanceEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AddressBalanceTest {

  @Test
  void fromEntityPositiveTest() {
    AddressBalanceEntity addressBalanceEntity = new AddressBalanceEntity();
    addressBalanceEntity.setAddress("address");
    addressBalanceEntity.setUnit("unit");
    addressBalanceEntity.setSlot(1L);
    addressBalanceEntity.setQuantity(BigInteger.ONE);

    AddressBalance addressBalance = AddressBalance.fromEntity(addressBalanceEntity);

    assertEquals("address", addressBalance.address());
    assertEquals("unit", addressBalance.unit());
    assertEquals(1L, addressBalance.slot());
    assertEquals(BigInteger.ONE, addressBalance.quantity());
  }

  @Test
  void fromEntityNullTest() {
    AddressBalanceEntity addressBalanceEntity = new AddressBalanceEntity();

    AddressBalance addressBalance = AddressBalance.fromEntity(addressBalanceEntity);

    assertNull(addressBalance.address());
    assertNull(addressBalance.unit());
    assertNull(addressBalance.slot());
    assertNull(addressBalance.quantity());
  }
}
