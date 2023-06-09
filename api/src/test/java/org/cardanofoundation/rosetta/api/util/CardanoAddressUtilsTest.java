package org.cardanofoundation.rosetta.api.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CardanoAddressUtilsTest {

  @Test
  void test_isStakeAddress_true_when_address_contains_stake() {
    String address = "stake1u80n7nvm3qlss9ls0krp5xh7sqxlazp8kz6n3fp5sgnul5cnxyg4p";
    assertTrue(CardanoAddressUtils.isStakeAddress(address));

  }
  @Test
  void test_isStakeAddress_true_when_address_contains_stake_test() {
    String address = "stake_test1u80n7nvm3qlss9ls0krp5xh7sqxlazp8kz6n3fp5sgnul5cnxyg4p";
    assertTrue(CardanoAddressUtils.isStakeAddress(address));

  }
  @Test
  void test_isStakeAddress_false_when_address_doenst_contain_stake_test() {
    String address = "pool1c8k78ny3xvsfgenhf4yzvpzwgzxmz0t0um0h2xnn2q83vjdr5dj";
    assertFalse(CardanoAddressUtils.isStakeAddress(address));

  }
}