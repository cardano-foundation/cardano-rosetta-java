package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The transaction_identifier uniquely identifies a transaction in a particular network and block or in the mempool.
 */

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class TransactionIdentifier {

  @JsonProperty("hash")
  private String hash;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionIdentifier transactionIdentifier = (TransactionIdentifier) o;
    return Objects.equals(this.hash, transactionIdentifier.hash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hash);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionIdentifier {\n");
    sb.append("    hash: ").append(toIndentedString(hash)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

