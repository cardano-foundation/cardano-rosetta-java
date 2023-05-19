package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CoinIdentifier;

/**
 * Coin contains its unique identifier and the amount it represents.
 */

@Schema(name = "Coin", description = "Coin contains its unique identifier and the amount it represents.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class Coin {

  @JsonProperty("coin_identifier")
  private CoinIdentifier coinIdentifier;

  @JsonProperty("amount")
  private Amount amount;

  @JsonProperty("metadata")
  @Valid
  private Map<String, List<TokenBundleItem>> metadata = null;

  public Coin coinIdentifier(CoinIdentifier coinIdentifier) {
    this.coinIdentifier = coinIdentifier;
    return this;
  }

  /**
   * Get coinIdentifier
   *
   * @return coinIdentifier
   */
  @NotNull
  @Valid
  @Schema(name = "coin_identifier", requiredMode = Schema.RequiredMode.REQUIRED)
  public CoinIdentifier getCoinIdentifier() {
    return coinIdentifier;
  }

  public void setCoinIdentifier(CoinIdentifier coinIdentifier) {
    this.coinIdentifier = coinIdentifier;
  }

  public Coin amount(Amount amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount
   *
   * @return amount
   */
  @NotNull
  @Valid
  @Schema(name = "amount", requiredMode = Schema.RequiredMode.REQUIRED)
  public Amount getAmount() {
    return amount;
  }

  public void setAmount(Amount amount) {
    this.amount = amount;
  }

  public Coin metadata(Map<String, List<TokenBundleItem>> metadata) {
    this.metadata = metadata;
    return this;
  }

  public Coin putMetadataItem(String key, List<TokenBundleItem> metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<>();
    }
    this.metadata.put(key, metadataItem);
    return this;
  }

  /**
   * Get metadata
   *
   * @return metadata
   */
  @Valid
  @Schema(name = "metadata", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public Map<String, List<TokenBundleItem>> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, List<TokenBundleItem>> metadata) {
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
    Coin coin = (Coin) o;
    return Objects.equals(this.coinIdentifier, coin.coinIdentifier) &&
        Objects.equals(this.amount, coin.amount) &&
        Objects.equals(this.metadata, coin.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(coinIdentifier, amount, metadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Coin {\n");
    sb.append("    coinIdentifier: ").append(toIndentedString(coinIdentifier)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first
   * line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

