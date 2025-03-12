package org.cardanofoundation.rosetta.common.enumeration;

import java.util.Arrays;

import lombok.Getter;

import static org.cardanofoundation.rosetta.common.util.Constants.*;

@Getter
public enum OperationType {

  INPUT(OPERATION_TYPE_INPUT),
  OUTPUT(OPERATION_TYPE_OUTPUT),
  STAKE_KEY_REGISTRATION(OPERATION_TYPE_STAKE_KEY_REGISTRATION),
  STAKE_DELEGATION(OPERATION_TYPE_STAKE_DELEGATION),
  WITHDRAWAL(OPERATION_TYPE_WITHDRAWAL),
  STAKE_KEY_DEREGISTRATION(OPERATION_TYPE_STAKE_KEY_DEREGISTRATION),
  POOL_REGISTRATION(OPERATION_TYPE_POOL_REGISTRATION),
  POOL_REGISTRATION_WITH_CERT(OPERATION_TYPE_POOL_REGISTRATION_WITH_CERT),
  POOL_RETIREMENT(OPERATION_TYPE_POOL_RETIREMENT),
  VOTE_REGISTRATION(OPERATION_TYPE_VOTE_REGISTRATION),

  VOTE_DREP_DELEGATION(OPERATION_TYPE_DREP_VOTE_DELEGATION); //

  private final String value;

  OperationType(String value) {
    this.value = value;
  }

  public static OperationType fromValue(String value) {
    return Arrays.stream(OperationType.values())
            .filter(ot -> ot.getValue().equals(value))
            .findFirst()
            .orElse(null);
            //.orElseThrow(ExceptionFactory::invalidOperationTypeError);
  }

}
