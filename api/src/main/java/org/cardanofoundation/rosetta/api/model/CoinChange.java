package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * CoinChange is used to represent a change in state of a some coin identified by a coin_identifier. This object is part of the Operation model and must be populated for UTXO-based blockchains. Coincidentally, this abstraction of UTXOs allows for supporting both account-based transfers and UTXO-based transfers on the same blockchain (when a transfer is account-based, don&#39;t populate this model).
 */

@Schema(name = "CoinChange", description = "CoinChange is used to represent a change in state of a some coin identified by a coin_identifier. This object is part of the Operation model and must be populated for UTXO-based blockchains. Coincidentally, this abstraction of UTXOs allows for supporting both account-based transfers and UTXO-based transfers on the same blockchain (when a transfer is account-based, don't populate this model).")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class CoinChange {

  @JsonProperty("coin_identifier")
  private CoinIdentifier coinIdentifier;

  @JsonProperty("coin_action")
  private CoinAction coinAction;

  public CoinChange coinIdentifier(CoinIdentifier coinIdentifier) {
    this.coinIdentifier = coinIdentifier;
    return this;
  }

  /**
   * Get coinIdentifier
   * @return coinIdentifier
  */
  @NotNull @Valid 
  @Schema(name = "coin_identifier", requiredMode = Schema.RequiredMode.REQUIRED)
  public CoinIdentifier getCoinIdentifier() {
    return coinIdentifier;
  }

  public void setCoinIdentifier(CoinIdentifier coinIdentifier) {
    this.coinIdentifier = coinIdentifier;
  }

  public CoinChange coinAction(CoinAction coinAction) {
    this.coinAction = coinAction;
    return this;
  }

  /**
   * Get coinAction
   * @return coinAction
  */
  @NotNull @Valid 
  @Schema(name = "coin_action", requiredMode = Schema.RequiredMode.REQUIRED)
  public CoinAction getCoinAction() {
    return coinAction;
  }

  public void setCoinAction(CoinAction coinAction) {
    this.coinAction = coinAction;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CoinChange coinChange = (CoinChange) o;
    return Objects.equals(this.coinIdentifier, coinChange.coinIdentifier) &&
        Objects.equals(this.coinAction, coinChange.coinAction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(coinIdentifier, coinAction);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CoinChange {\n");
    sb.append("    coinIdentifier: ").append(toIndentedString(coinIdentifier)).append("\n");
    sb.append("    coinAction: ").append(toIndentedString(coinAction)).append("\n");
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

