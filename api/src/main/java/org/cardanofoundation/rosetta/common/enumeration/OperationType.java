package org.cardanofoundation.rosetta.common.enumeration;

import com.google.common.base.Enums;
import lombok.Getter;

import org.cardanofoundation.rosetta.common.util.Constants;

@Getter
public enum OperationType {

  INPUT(Constants.OPERATION_TYPE_INPUT),
  OUTPUT(Constants.OPERATION_TYPE_OUTPUT),
  STAKE_KEY_REGISTRATION(Constants.OPERATION_TYPE_STAKE_KEY_REGISTRATION),
  STAKE_DELEGATION(Constants.OPERATION_TYPE_STAKE_DELEGATION),
  WITHDRAWAL(Constants.OPERATION_TYPE_WITHDRAWAL),
  STAKE_KEY_DEREGISTRATION(Constants.OPERATION_TYPE_STAKE_KEY_DEREGISTRATION),
  POOL_REGISTRATION(Constants.OPERATION_TYPE_POOL_REGISTRATION),
  POOL_REGISTRATION_WITH_CERT(Constants.OPERATION_TYPE_POOL_REGISTRATION_WITH_CERT),
  POOL_RETIREMENT(Constants.OPERATION_TYPE_POOL_RETIREMENT),
  VOTE_REGISTRATION(Constants.OPERATION_TYPE_VOTE_REGISTRATION),

  VOTE_DREP_DELEGATION(Constants.OPERATION_TYPE_DREP_VOTE_DELEGATION); //

  private final String value;

  OperationType(String value) {
    this.value = value;
  }

  public static OperationType fromValue(String value) {
    return Enums.getIfPresent(OperationType.class, value).orNull();

    for (OperationType b : OperationType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    return null;
  }
}
