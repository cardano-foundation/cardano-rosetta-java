package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Operations contain all balance-changing information within a transaction. They are always one-sided (only affect 1 AccountIdentifier) and can succeed or fail independently from a Transaction. Operations are used both to represent on-chain data (Data API) and to construct new transactions (Construction API), creating a standard interface for reading and writing to blockchains.
 */

@Schema(name = "Operation", description = "Operations contain all balance-changing information within a transaction. They are always one-sided (only affect 1 AccountIdentifier) and can succeed or fail independently from a Transaction. Operations are used both to represent on-chain data (Data API) and to construct new transactions (Construction API), creating a standard interface for reading and writing to blockchains.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class Operation {

  @JsonProperty("operation_identifier")
  private OperationIdentifier operationIdentifier;

  @JsonProperty("related_operations")
  @Valid
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

  public Operation operationIdentifier(OperationIdentifier operationIdentifier) {
    this.operationIdentifier = operationIdentifier;
    return this;
  }

  /**
   * Get operationIdentifier
   * @return operationIdentifier
  */
  @NotNull @Valid 
  @Schema(name = "operation_identifier", requiredMode = Schema.RequiredMode.REQUIRED)
  public OperationIdentifier getOperationIdentifier() {
    return operationIdentifier;
  }

  public void setOperationIdentifier(OperationIdentifier operationIdentifier) {
    this.operationIdentifier = operationIdentifier;
  }

  public Operation relatedOperations(List<OperationIdentifier> relatedOperations) {
    this.relatedOperations = relatedOperations;
    return this;
  }

  public Operation addRelatedOperationsItem(OperationIdentifier relatedOperationsItem) {
    if (this.relatedOperations == null) {
      this.relatedOperations = new ArrayList<>();
    }
    this.relatedOperations.add(relatedOperationsItem);
    return this;
  }

  /**
   * Restrict referenced related_operations to identifier indices < the current operation_identifier.index. This ensures there exists a clear DAG-structure of relations. Since operations are one-sided, one could imagine relating operations in a single transfer or linking operations in a call tree.
   * @return relatedOperations
  */
  @Valid 
  @Schema(name = "related_operations", example = "[{\"index\":1},{\"index\":2}]", description = "Restrict referenced related_operations to identifier indices < the current operation_identifier.index. This ensures there exists a clear DAG-structure of relations. Since operations are one-sided, one could imagine relating operations in a single transfer or linking operations in a call tree.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public List<OperationIdentifier> getRelatedOperations() {
    return relatedOperations;
  }

  public void setRelatedOperations(List<OperationIdentifier> relatedOperations) {
    this.relatedOperations = relatedOperations;
  }

  public Operation type(String type) {
    this.type = type;
    return this;
  }

  /**
   * Type is the network-specific type of the operation. Ensure that any type that can be returned here is also specified in the NetworkOptionsResponse. This can be very useful to downstream consumers that parse all block data.
   * @return type
  */
  @NotNull 
  @Schema(name = "type", example = "Transfer", description = "Type is the network-specific type of the operation. Ensure that any type that can be returned here is also specified in the NetworkOptionsResponse. This can be very useful to downstream consumers that parse all block data.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Operation status(String status) {
    this.status = status;
    return this;
  }

  /**
   * Status is the network-specific status of the operation. Status is not defined on the transaction object because blockchains with smart contracts may have transactions that partially apply (some operations are successful and some are not). Blockchains with atomic transactions (all operations succeed or all operations fail) will have the same status for each operation. On-chain operations (operations retrieved in the `/block` and `/block/transaction` endpoints) MUST have a populated status field (anything on-chain must have succeeded or failed). However, operations provided during transaction construction (often times called \"intent\" in the documentation) MUST NOT have a populated status field (operations yet to be included on-chain have not yet succeeded or failed).
   * @return status
  */
  
  @Schema(name = "status", example = "Reverted", description = "Status is the network-specific status of the operation. Status is not defined on the transaction object because blockchains with smart contracts may have transactions that partially apply (some operations are successful and some are not). Blockchains with atomic transactions (all operations succeed or all operations fail) will have the same status for each operation. On-chain operations (operations retrieved in the `/block` and `/block/transaction` endpoints) MUST have a populated status field (anything on-chain must have succeeded or failed). However, operations provided during transaction construction (often times called \"intent\" in the documentation) MUST NOT have a populated status field (operations yet to be included on-chain have not yet succeeded or failed).", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Operation account(AccountIdentifier account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
  */
  @Valid 
  @Schema(name = "account", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public AccountIdentifier getAccount() {
    return account;
  }

  public void setAccount(AccountIdentifier account) {
    this.account = account;
  }

  public Operation amount(Amount amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount
   * @return amount
  */
  @Valid 
  @Schema(name = "amount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public Amount getAmount() {
    return amount;
  }

  public void setAmount(Amount amount) {
    this.amount = amount;
  }

  public Operation coinChange(CoinChange coinChange) {
    this.coinChange = coinChange;
    return this;
  }

  /**
   * Get coinChange
   * @return coinChange
  */
  @Valid 
  @Schema(name = "coin_change", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public CoinChange getCoinChange() {
    return coinChange;
  }

  public void setCoinChange(CoinChange coinChange) {
    this.coinChange = coinChange;
  }

  public Operation metadata(OperationMetadata metadata) {
    this.metadata = metadata;
    return this;
  }

  /**
   * Get metadata
   * @return metadata
  */
  @Valid 
  @Schema(name = "metadata", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public OperationMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(OperationMetadata metadata) {
    this.metadata = metadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Operation operation = (Operation) o;
    return Objects.equals(this.operationIdentifier, operation.operationIdentifier) &&
        Objects.equals(this.relatedOperations, operation.relatedOperations) &&
        Objects.equals(this.type, operation.type) &&
        Objects.equals(this.status, operation.status) &&
        Objects.equals(this.account, operation.account) &&
        Objects.equals(this.amount, operation.amount) &&
        Objects.equals(this.coinChange, operation.coinChange) &&
        Objects.equals(this.metadata, operation.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(operationIdentifier, relatedOperations, type, status, account, amount, coinChange, metadata);
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

