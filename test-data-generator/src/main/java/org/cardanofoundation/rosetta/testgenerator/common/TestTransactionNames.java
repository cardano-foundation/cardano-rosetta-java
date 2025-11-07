package org.cardanofoundation.rosetta.testgenerator.common;

public enum TestTransactionNames {
  // Transaction names for SimpleTransactions
  SIMPLE_TRANSACTION("simple_transaction"),
  SIMPLE_LOVELACE_FIRST_TRANSACTION("simple_first_lovelace_transaction"),
  SIMPLE_LOVELACE_SECOND_TRANSACTION("simple_second_lovelace_transaction"),
  SIMPLE_NEW_COINS_TRANSACTION("simple_new_coins_transaction"),
  SIMPLE_NEW_COINS_STAKE_POOL_TRANSACTION("simple_new_coins_stake_pool_transaction"),
  SIMPLE_NEW_EMPTY_NAME_COINS_TRANSACTION("simple_new_empty_name_coins_transaction"),
  STAKE_KEY_REGISTRATION_TRANSACTION("stake_key_registration"),
  STAKE_KEY_DEREGISTRATION_TRANSACTION("stake_key_deregistration"),

  // Transaction names for PoolTransactions
  POOL_REGISTRATION_TRANSACTION("pool_registration"),
  POOL_DELEGATION_TRANSACTION("pool_delegation"),
  POOL_RETIREMENT_TRANSACTION("pool_retirement"),

  // Transaction names for GovernanceTransactions
  DREP_REGISTER("drep_register"),
  DREP_VOTE_DELEGATION("drep_vote_delegation");

  private final String name;

  TestTransactionNames(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
