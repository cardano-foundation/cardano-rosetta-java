package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Metadata related to Cardano operations
 */

@Schema(name = "OperationMetadata", description = "Metadata related to Cardano operations")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class OperationMetadata {

  @JsonProperty("withdrawalAmount")
  private Amount withdrawalAmount;

  @JsonProperty("depositAmount")
  private Amount depositAmount;

  @JsonProperty("refundAmount")
  private Amount refundAmount;

  @JsonProperty("staking_credential")
  private PublicKey stakingCredential;

  @JsonProperty("pool_key_hash")
  private String poolKeyHash;

  @JsonProperty("epoch")
  private BigDecimal epoch;

  @JsonProperty("tokenBundle")
  @Valid
  private List<TokenBundleItem> tokenBundle = null;

  @JsonProperty("poolRegistrationCert")
  private String poolRegistrationCert;

  @JsonProperty("poolRegistrationParams")
  private PoolRegistrationParams poolRegistrationParams;

  @JsonProperty("voteRegistrationMetadata")
  private VoteRegistrationMetadata voteRegistrationMetadata;

  public OperationMetadata withdrawalAmount(Amount withdrawalAmount) {
    this.withdrawalAmount = withdrawalAmount;
    return this;
  }

  /**
   * Get withdrawalAmount
   * @return withdrawalAmount
  */
  @Valid 
  @Schema(name = "withdrawalAmount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public Amount getWithdrawalAmount() {
    return withdrawalAmount;
  }

  public void setWithdrawalAmount(Amount withdrawalAmount) {
    this.withdrawalAmount = withdrawalAmount;
  }

  public OperationMetadata depositAmount(Amount depositAmount) {
    this.depositAmount = depositAmount;
    return this;
  }

  /**
   * Get depositAmount
   * @return depositAmount
  */
  @Valid 
  @Schema(name = "depositAmount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public Amount getDepositAmount() {
    return depositAmount;
  }

  public void setDepositAmount(Amount depositAmount) {
    this.depositAmount = depositAmount;
  }

  public OperationMetadata refundAmount(Amount refundAmount) {
    this.refundAmount = refundAmount;
    return this;
  }

  /**
   * Get refundAmount
   * @return refundAmount
  */
  @Valid 
  @Schema(name = "refundAmount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public Amount getRefundAmount() {
    return refundAmount;
  }

  public void setRefundAmount(Amount refundAmount) {
    this.refundAmount = refundAmount;
  }

  public OperationMetadata stakingCredential(PublicKey stakingCredential) {
    this.stakingCredential = stakingCredential;
    return this;
  }

  /**
   * Get stakingCredential
   * @return stakingCredential
  */
  @Valid 
  @Schema(name = "staking_credential", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public PublicKey getStakingCredential() {
    return stakingCredential;
  }

  public void setStakingCredential(PublicKey stakingCredential) {
    this.stakingCredential = stakingCredential;
  }

  public OperationMetadata poolKeyHash(String poolKeyHash) {
    this.poolKeyHash = poolKeyHash;
    return this;
  }

  /**
   * Get poolKeyHash
   * @return poolKeyHash
  */
  
  @Schema(name = "pool_key_hash", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getPoolKeyHash() {
    return poolKeyHash;
  }

  public void setPoolKeyHash(String poolKeyHash) {
    this.poolKeyHash = poolKeyHash;
  }

  public OperationMetadata epoch(BigDecimal epoch) {
    this.epoch = epoch;
    return this;
  }

  /**
   * Get epoch
   * @return epoch
  */
  @Valid 
  @Schema(name = "epoch", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public BigDecimal getEpoch() {
    return epoch;
  }

  public void setEpoch(BigDecimal epoch) {
    this.epoch = epoch;
  }

  public OperationMetadata tokenBundle(List<TokenBundleItem> tokenBundle) {
    this.tokenBundle = tokenBundle;
    return this;
  }

  public OperationMetadata addTokenBundleItem(TokenBundleItem tokenBundleItem) {
    if (this.tokenBundle == null) {
      this.tokenBundle = new ArrayList<>();
    }
    this.tokenBundle.add(tokenBundleItem);
    return this;
  }

  /**
   * A token bundle is a heterogeneous (‘mixed’) collection of tokens. Any tokens can be bundled together. Token bundles are the standard - and only - way to represent and store assets on the Cardano blockchain.
   * @return tokenBundle
  */
  @Valid 
  @Schema(name = "tokenBundle", description = "A token bundle is a heterogeneous (‘mixed’) collection of tokens. Any tokens can be bundled together. Token bundles are the standard - and only - way to represent and store assets on the Cardano blockchain.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public List<TokenBundleItem> getTokenBundle() {
    return tokenBundle;
  }

  public void setTokenBundle(List<TokenBundleItem> tokenBundle) {
    this.tokenBundle = tokenBundle;
  }

  public OperationMetadata poolRegistrationCert(String poolRegistrationCert) {
    this.poolRegistrationCert = poolRegistrationCert;
    return this;
  }

  /**
   * Certificate of a pool registration encoded as hex string
   * @return poolRegistrationCert
  */
  
  @Schema(name = "poolRegistrationCert", description = "Certificate of a pool registration encoded as hex string", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getPoolRegistrationCert() {
    return poolRegistrationCert;
  }

  public void setPoolRegistrationCert(String poolRegistrationCert) {
    this.poolRegistrationCert = poolRegistrationCert;
  }

  public OperationMetadata poolRegistrationParams(PoolRegistrationParams poolRegistrationParams) {
    this.poolRegistrationParams = poolRegistrationParams;
    return this;
  }

  /**
   * Get poolRegistrationParams
   * @return poolRegistrationParams
  */
  @Valid 
  @Schema(name = "poolRegistrationParams", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public PoolRegistrationParams getPoolRegistrationParams() {
    return poolRegistrationParams;
  }

  public void setPoolRegistrationParams(PoolRegistrationParams poolRegistrationParams) {
    this.poolRegistrationParams = poolRegistrationParams;
  }

  public OperationMetadata voteRegistrationMetadata(VoteRegistrationMetadata voteRegistrationMetadata) {
    this.voteRegistrationMetadata = voteRegistrationMetadata;
    return this;
  }

  /**
   * Get voteRegistrationMetadata
   * @return voteRegistrationMetadata
  */
  @Valid 
  @Schema(name = "voteRegistrationMetadata", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public VoteRegistrationMetadata getVoteRegistrationMetadata() {
    return voteRegistrationMetadata;
  }

  public void setVoteRegistrationMetadata(VoteRegistrationMetadata voteRegistrationMetadata) {
    this.voteRegistrationMetadata = voteRegistrationMetadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationMetadata operationMetadata = (OperationMetadata) o;
    return Objects.equals(this.withdrawalAmount, operationMetadata.withdrawalAmount) &&
        Objects.equals(this.depositAmount, operationMetadata.depositAmount) &&
        Objects.equals(this.refundAmount, operationMetadata.refundAmount) &&
        Objects.equals(this.stakingCredential, operationMetadata.stakingCredential) &&
        Objects.equals(this.poolKeyHash, operationMetadata.poolKeyHash) &&
        Objects.equals(this.epoch, operationMetadata.epoch) &&
        Objects.equals(this.tokenBundle, operationMetadata.tokenBundle) &&
        Objects.equals(this.poolRegistrationCert, operationMetadata.poolRegistrationCert) &&
        Objects.equals(this.poolRegistrationParams, operationMetadata.poolRegistrationParams) &&
        Objects.equals(this.voteRegistrationMetadata, operationMetadata.voteRegistrationMetadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(withdrawalAmount, depositAmount, refundAmount, stakingCredential, poolKeyHash, epoch, tokenBundle, poolRegistrationCert, poolRegistrationParams, voteRegistrationMetadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationMetadata {\n");
    sb.append("    withdrawalAmount: ").append(toIndentedString(withdrawalAmount)).append("\n");
    sb.append("    depositAmount: ").append(toIndentedString(depositAmount)).append("\n");
    sb.append("    refundAmount: ").append(toIndentedString(refundAmount)).append("\n");
    sb.append("    stakingCredential: ").append(toIndentedString(stakingCredential)).append("\n");
    sb.append("    poolKeyHash: ").append(toIndentedString(poolKeyHash)).append("\n");
    sb.append("    epoch: ").append(toIndentedString(epoch)).append("\n");
    sb.append("    tokenBundle: ").append(toIndentedString(tokenBundle)).append("\n");
    sb.append("    poolRegistrationCert: ").append(toIndentedString(poolRegistrationCert)).append("\n");
    sb.append("    poolRegistrationParams: ").append(toIndentedString(poolRegistrationParams)).append("\n");
    sb.append("    voteRegistrationMetadata: ").append(toIndentedString(voteRegistrationMetadata)).append("\n");
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

