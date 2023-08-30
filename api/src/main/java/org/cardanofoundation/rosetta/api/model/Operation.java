package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operation {

  @JsonProperty("operation_identifier")
  private OperationIdentifier operationIdentifier;

  @JsonProperty("related_operations")
  private List<OperationIdentifier> relatedOperations = null;

  @JsonProperty("type")
  private String type;

  @JsonProperty("status")
  private String status;

  @JsonProperty("account")
  private AccountIdentifier account;

  @JsonProperty("amount")
  private Amount amount;

  @JsonProperty("coin_change")
  private CoinChange coinChange;

  @JsonProperty("metadata")
  private OperationMetadata metadata;


  public Operation(OperationIdentifier operationIdentifier, String type, String status, OperationMetadata metadata) {
    this.operationIdentifier = operationIdentifier;
    this.type = type;
    this.status = status;
    this.metadata = metadata;
  }

  public Operation(OperationIdentifier operationIdentifier, String type, String status, AccountIdentifier account, Amount amount, OperationMetadata metadata) {
    this.operationIdentifier = operationIdentifier;
    this.type = type;
    this.status = status;
    this.account = account;
    this.amount = amount;
    this.metadata = metadata;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Operation {\n");
    sb.append("    operationIdentifier: ").append(toIndentedString(operationIdentifier)).append("\n");
    sb.append("    relatedOperations: ").append(toIndentedString(relatedOperations)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    coinChange: ").append(toIndentedString(coinChange)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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

