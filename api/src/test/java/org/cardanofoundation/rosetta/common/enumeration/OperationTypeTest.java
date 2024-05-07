package org.cardanofoundation.rosetta.common.enumeration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OperationTypeTest {

  final OperationType[] operationTypes = OperationType.values();

  @Test
  void fromValue() {
    for (OperationType operationType : operationTypes) {
      OperationType actual = OperationType.fromValue(operationType.getValue());
      assertEquals(operationType, actual);
    }
    assertNull(OperationType.fromValue("Invalid"));
  }
}
