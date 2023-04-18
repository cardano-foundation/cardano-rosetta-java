package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Catalyst registration metadata format
 */

@Schema(name = "VoteRegistrationMetadata", description = "Catalyst registration metadata format")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class VoteRegistrationMetadata {

  @JsonProperty("stakeKey")
  private PublicKey stakeKey;

  @JsonProperty("votingKey")
  private PublicKey votingKey;

  @JsonProperty("rewardAddress")
  private String rewardAddress;

  @JsonProperty("votingNonce")
  private BigDecimal votingNonce;

  @JsonProperty("votingSignature")
  private String votingSignature;

  public VoteRegistrationMetadata stakeKey(PublicKey stakeKey) {
    this.stakeKey = stakeKey;
    return this;
  }

  /**
   * Get stakeKey
   * @return stakeKey
  */
  @NotNull @Valid 
  @Schema(name = "stakeKey", requiredMode = Schema.RequiredMode.REQUIRED)
  public PublicKey getStakeKey() {
    return stakeKey;
  }

  public void setStakeKey(PublicKey stakeKey) {
    this.stakeKey = stakeKey;
  }

  public VoteRegistrationMetadata votingKey(PublicKey votingKey) {
    this.votingKey = votingKey;
    return this;
  }

  /**
   * Get votingKey
   * @return votingKey
  */
  @NotNull @Valid 
  @Schema(name = "votingKey", requiredMode = Schema.RequiredMode.REQUIRED)
  public PublicKey getVotingKey() {
    return votingKey;
  }

  public void setVotingKey(PublicKey votingKey) {
    this.votingKey = votingKey;
  }

  public VoteRegistrationMetadata rewardAddress(String rewardAddress) {
    this.rewardAddress = rewardAddress;
    return this;
  }

  /**
   * Shelley address to receive rewards
   * @return rewardAddress
  */
  @NotNull 
  @Schema(name = "rewardAddress", description = "Shelley address to receive rewards", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getRewardAddress() {
    return rewardAddress;
  }

  public void setRewardAddress(String rewardAddress) {
    this.rewardAddress = rewardAddress;
  }

  public VoteRegistrationMetadata votingNonce(BigDecimal votingNonce) {
    this.votingNonce = votingNonce;
    return this;
  }

  /**
   * Unsigned integer (of CBOR major type 0). Current slot number
   * @return votingNonce
  */
  @NotNull @Valid 
  @Schema(name = "votingNonce", description = "Unsigned integer (of CBOR major type 0). Current slot number", requiredMode = Schema.RequiredMode.REQUIRED)
  public BigDecimal getVotingNonce() {
    return votingNonce;
  }

  public void setVotingNonce(BigDecimal votingNonce) {
    this.votingNonce = votingNonce;
  }

  public VoteRegistrationMetadata votingSignature(String votingSignature) {
    this.votingSignature = votingSignature;
    return this;
  }

  /**
   * ED25119 signature CBOR byte array of blake2b-256 hash of the registration metadata signed using the staking key
   * @return votingSignature
  */
  @NotNull 
  @Schema(name = "votingSignature", description = "ED25119 signature CBOR byte array of blake2b-256 hash of the registration metadata signed using the staking key", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getVotingSignature() {
    return votingSignature;
  }

  public void setVotingSignature(String votingSignature) {
    this.votingSignature = votingSignature;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VoteRegistrationMetadata voteRegistrationMetadata = (VoteRegistrationMetadata) o;
    return Objects.equals(this.stakeKey, voteRegistrationMetadata.stakeKey) &&
        Objects.equals(this.votingKey, voteRegistrationMetadata.votingKey) &&
        Objects.equals(this.rewardAddress, voteRegistrationMetadata.rewardAddress) &&
        Objects.equals(this.votingNonce, voteRegistrationMetadata.votingNonce) &&
        Objects.equals(this.votingSignature, voteRegistrationMetadata.votingSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stakeKey, votingKey, rewardAddress, votingNonce, votingSignature);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VoteRegistrationMetadata {\n");
    sb.append("    stakeKey: ").append(toIndentedString(stakeKey)).append("\n");
    sb.append("    votingKey: ").append(toIndentedString(votingKey)).append("\n");
    sb.append("    rewardAddress: ").append(toIndentedString(rewardAddress)).append("\n");
    sb.append("    votingNonce: ").append(toIndentedString(votingNonce)).append("\n");
    sb.append("    votingSignature: ").append(toIndentedString(votingSignature)).append("\n");
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

