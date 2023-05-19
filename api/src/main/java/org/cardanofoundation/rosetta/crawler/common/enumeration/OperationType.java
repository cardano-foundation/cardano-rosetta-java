package org.cardanofoundation.rosetta.crawler.common.enumeration;

public enum OperationType {
  INPUT("input"),
  OUTPUT("output"),
  STAKE_KEY_REGISTRATION("stakeKeyRegistration"),
  STAKE_DELEGATION("stakeDelegation"),
  WITHDRAWAL("withdrawal"),
  STAKE_KEY_DEREGISTRATION("stakeKeyDeregistration"),
  POOL_REGISTRATION("poolRegistration"),
  POOL_REGISTRATION_WITH_CERT("poolRegistrationWithCert"),
  POOL_RETIREMENT("poolRetirement"),
  VOTE_REGISTRATION("voteRegistration");

  private final String value;

  OperationType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}