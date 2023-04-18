package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * The operation_identifier uniquely identifies an operation within a transaction.
 */

@Schema(name = "OperationIdentifier", description = "The operation_identifier uniquely identifies an operation within a transaction.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class OperationIdentifier {

  @JsonProperty("index")
  private Long index;

  @JsonProperty("network_index")
  private Long networkIndex;

  public OperationIdentifier index(Long index) {
    this.index = index;
    return this;
  }

  /**
   * The operation index is used to ensure each operation has a unique identifier within a transaction. This index is only relative to the transaction and NOT GLOBAL. The operations in each transaction should start from index 0. To clarify, there may not be any notion of an operation index in the blockchain being described.
   * minimum: 0
   * @return index
  */
  @NotNull @Min(0L) 
  @Schema(name = "index", example = "5", description = "The operation index is used to ensure each operation has a unique identifier within a transaction. This index is only relative to the transaction and NOT GLOBAL. The operations in each transaction should start from index 0. To clarify, there may not be any notion of an operation index in the blockchain being described.", requiredMode = Schema.RequiredMode.REQUIRED)
  public Long getIndex() {
    return index;
  }

  public void setIndex(Long index) {
    this.index = index;
  }

  public OperationIdentifier networkIndex(Long networkIndex) {
    this.networkIndex = networkIndex;
    return this;
  }

  /**
   * Some blockchains specify an operation index that is essential for client use. For example, Bitcoin uses a network_index to identify which UTXO was used in a transaction. network_index should not be populated if there is no notion of an operation index in a blockchain (typically most account-based blockchains).
   * minimum: 0
   * @return networkIndex
  */
  @Min(0L) 
  @Schema(name = "network_index", example = "0", description = "Some blockchains specify an operation index that is essential for client use. For example, Bitcoin uses a network_index to identify which UTXO was used in a transaction. network_index should not be populated if there is no notion of an operation index in a blockchain (typically most account-based blockchains).", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public Long getNetworkIndex() {
    return networkIndex;
  }

  public void setNetworkIndex(Long networkIndex) {
    this.networkIndex = networkIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationIdentifier operationIdentifier = (OperationIdentifier) o;
    return Objects.equals(this.index, operationIdentifier.index) &&
        Objects.equals(this.networkIndex, operationIdentifier.networkIndex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, networkIndex);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationIdentifier {\n");
    sb.append("    index: ").append(toIndentedString(index)).append("\n");
    sb.append("    networkIndex: ").append(toIndentedString(networkIndex)).append("\n");
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

