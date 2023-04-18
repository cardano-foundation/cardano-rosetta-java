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
 * PoolRegistrationParams
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class PoolRegistrationParams {

  @JsonProperty("vrfKeyHash")
  private String vrfKeyHash;

  @JsonProperty("rewardAddress")
  private String rewardAddress;

  @JsonProperty("pledge")
  private String pledge;

  @JsonProperty("cost")
  private String cost;

  @JsonProperty("poolOwners")
  @Valid
  private List<String> poolOwners = new ArrayList<>();

  @JsonProperty("relays")
  @Valid
  private List<Relay1> relays = new ArrayList<>();

  @JsonProperty("margin")
  private PoolMargin margin;

  @JsonProperty("margin_percentage")
  private String marginPercentage;

  @JsonProperty("poolMetadata")
  private PoolMetadata poolMetadata;

  public PoolRegistrationParams vrfKeyHash(String vrfKeyHash) {
    this.vrfKeyHash = vrfKeyHash;
    return this;
  }

  /**
   * Get vrfKeyHash
   * @return vrfKeyHash
  */
  @NotNull 
  @Schema(name = "vrfKeyHash", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getVrfKeyHash() {
    return vrfKeyHash;
  }

  public void setVrfKeyHash(String vrfKeyHash) {
    this.vrfKeyHash = vrfKeyHash;
  }

  public PoolRegistrationParams rewardAddress(String rewardAddress) {
    this.rewardAddress = rewardAddress;
    return this;
  }

  /**
   * Get rewardAddress
   * @return rewardAddress
  */
  @NotNull 
  @Schema(name = "rewardAddress", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getRewardAddress() {
    return rewardAddress;
  }

  public void setRewardAddress(String rewardAddress) {
    this.rewardAddress = rewardAddress;
  }

  public PoolRegistrationParams pledge(String pledge) {
    this.pledge = pledge;
    return this;
  }

  /**
   * Lovelace amount to pledge
   * @return pledge
  */
  @NotNull 
  @Schema(name = "pledge", description = "Lovelace amount to pledge", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getPledge() {
    return pledge;
  }

  public void setPledge(String pledge) {
    this.pledge = pledge;
  }

  public PoolRegistrationParams cost(String cost) {
    this.cost = cost;
    return this;
  }

  /**
   * Operational costs per epoch lovelace
   * @return cost
  */
  @NotNull 
  @Schema(name = "cost", description = "Operational costs per epoch lovelace", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getCost() {
    return cost;
  }

  public void setCost(String cost) {
    this.cost = cost;
  }

  public PoolRegistrationParams poolOwners(List<String> poolOwners) {
    this.poolOwners = poolOwners;
    return this;
  }

  public PoolRegistrationParams addPoolOwnersItem(String poolOwnersItem) {
    this.poolOwners.add(poolOwnersItem);
    return this;
  }

  /**
   * Get poolOwners
   * @return poolOwners
  */
  @NotNull 
  @Schema(name = "poolOwners", requiredMode = Schema.RequiredMode.REQUIRED)
  public List<String> getPoolOwners() {
    return poolOwners;
  }

  public void setPoolOwners(List<String> poolOwners) {
    this.poolOwners = poolOwners;
  }

  public PoolRegistrationParams relays(List<Relay1> relays) {
    this.relays = relays;
    return this;
  }

  public PoolRegistrationParams addRelaysItem(Relay1 relaysItem) {
    this.relays.add(relaysItem);
    return this;
  }

  /**
   * Get relays
   * @return relays
  */
  @NotNull @Valid 
  @Schema(name = "relays", requiredMode = Schema.RequiredMode.REQUIRED)
  public List<Relay1> getRelays() {
    return relays;
  }

  public void setRelays(List<Relay1> relays) {
    this.relays = relays;
  }

  public PoolRegistrationParams margin(PoolMargin margin) {
    this.margin = margin;
    return this;
  }

  /**
   * Get margin
   * @return margin
  */
  @Valid 
  @Schema(name = "margin", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public PoolMargin getMargin() {
    return margin;
  }

  public void setMargin(PoolMargin margin) {
    this.margin = margin;
  }

  public PoolRegistrationParams marginPercentage(String marginPercentage) {
    this.marginPercentage = marginPercentage;
    return this;
  }

  /**
   * Get marginPercentage
   * @return marginPercentage
  */
  
  @Schema(name = "margin_percentage", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getMarginPercentage() {
    return marginPercentage;
  }

  public void setMarginPercentage(String marginPercentage) {
    this.marginPercentage = marginPercentage;
  }

  public PoolRegistrationParams poolMetadata(PoolMetadata poolMetadata) {
    this.poolMetadata = poolMetadata;
    return this;
  }

  /**
   * Get poolMetadata
   * @return poolMetadata
  */
  @Valid 
  @Schema(name = "poolMetadata", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public PoolMetadata getPoolMetadata() {
    return poolMetadata;
  }

  public void setPoolMetadata(PoolMetadata poolMetadata) {
    this.poolMetadata = poolMetadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PoolRegistrationParams poolRegistrationParams = (PoolRegistrationParams) o;
    return Objects.equals(this.vrfKeyHash, poolRegistrationParams.vrfKeyHash) &&
        Objects.equals(this.rewardAddress, poolRegistrationParams.rewardAddress) &&
        Objects.equals(this.pledge, poolRegistrationParams.pledge) &&
        Objects.equals(this.cost, poolRegistrationParams.cost) &&
        Objects.equals(this.poolOwners, poolRegistrationParams.poolOwners) &&
        Objects.equals(this.relays, poolRegistrationParams.relays) &&
        Objects.equals(this.margin, poolRegistrationParams.margin) &&
        Objects.equals(this.marginPercentage, poolRegistrationParams.marginPercentage) &&
        Objects.equals(this.poolMetadata, poolRegistrationParams.poolMetadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vrfKeyHash, rewardAddress, pledge, cost, poolOwners, relays, margin, marginPercentage, poolMetadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PoolRegistrationParams {\n");
    sb.append("    vrfKeyHash: ").append(toIndentedString(vrfKeyHash)).append("\n");
    sb.append("    rewardAddress: ").append(toIndentedString(rewardAddress)).append("\n");
    sb.append("    pledge: ").append(toIndentedString(pledge)).append("\n");
    sb.append("    cost: ").append(toIndentedString(cost)).append("\n");
    sb.append("    poolOwners: ").append(toIndentedString(poolOwners)).append("\n");
    sb.append("    relays: ").append(toIndentedString(relays)).append("\n");
    sb.append("    margin: ").append(toIndentedString(margin)).append("\n");
    sb.append("    marginPercentage: ").append(toIndentedString(marginPercentage)).append("\n");
    sb.append("    poolMetadata: ").append(toIndentedString(poolMetadata)).append("\n");
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

