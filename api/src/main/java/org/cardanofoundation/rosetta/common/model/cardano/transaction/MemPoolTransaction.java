package org.cardanofoundation.rosetta.common.model.cardano.transaction;

import java.util.Arrays;
import java.util.Objects;

import org.openapitools.client.model.TransactionIdentifier;

public record MemPoolTransaction(TransactionIdentifier identifier, byte[] txBytes) {

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    MemPoolTransaction that = (MemPoolTransaction) object;
    return Objects.equals(identifier, that.identifier) && Arrays.equals(txBytes,
        that.txBytes);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(identifier);
    result = 31 * result + Arrays.hashCode(txBytes);
    return result;
  }

  @Override
  public String toString() {
    return "MemPoolTransaction{" +
        "identifier=" + identifier +
        ", txBytes=" + Arrays.toString(txBytes) +
        '}';
  }
}
