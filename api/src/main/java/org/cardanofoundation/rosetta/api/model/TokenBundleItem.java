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
 * TokenBundleItem
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class TokenBundleItem {

  @JsonProperty("policyId")
  private String policyId;

  @JsonProperty("tokens")
  @Valid
  private List<Amount> tokens = new ArrayList<>();

  public TokenBundleItem policyId(String policyId) {
    this.policyId = policyId;
    return this;
  }

  /**
   * Policy Id hex string
   * @return policyId
  */
  @NotNull 
  @Schema(name = "policyId", description = "Policy Id hex string", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getPolicyId() {
    return policyId;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public TokenBundleItem tokens(List<Amount> tokens) {
    this.tokens = tokens;
    return this;
  }

  public TokenBundleItem addTokensItem(Amount tokensItem) {
    this.tokens.add(tokensItem);
    return this;
  }

  /**
   * Get tokens
   * @return tokens
  */
  @NotNull @Valid 
  @Schema(name = "tokens", requiredMode = Schema.RequiredMode.REQUIRED)
  public List<Amount> getTokens() {
    return tokens;
  }

  public void setTokens(List<Amount> tokens) {
    this.tokens = tokens;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TokenBundleItem tokenBundleItem = (TokenBundleItem) o;
    return Objects.equals(this.policyId, tokenBundleItem.policyId) &&
        Objects.equals(this.tokens, tokenBundleItem.tokens);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyId, tokens);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TokenBundleItem {\n");
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    tokens: ").append(toIndentedString(tokens)).append("\n");
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

