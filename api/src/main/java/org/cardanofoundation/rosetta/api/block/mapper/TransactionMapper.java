package org.cardanofoundation.rosetta.api.block.mapper;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.*;
import org.cardanofoundation.rosetta.api.block.model.entity.*;
import org.cardanofoundation.rosetta.common.mapper.util.BaseMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Map;

@Mapper(config = BaseMapper.class, uses = {TransactionMapperUtils.class})
public interface TransactionMapper {

  @Mapping(target = "owners", source = "poolOwners")
  PoolRegistration mapEntityToPoolRegistration(PoolRegistrationEntity entity);

  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_POOL_REGISTRATION)
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.poolId")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "metadata.depositAmount", expression = "java(transactionMapperUtils.getDepositAmountPool())")
  @Mapping(target = "metadata.poolRegistrationParams.pledge", source = "model.pledge")
  @Mapping(target = "metadata.poolRegistrationParams.cost", source = "model.cost")
  @Mapping(target = "metadata.poolRegistrationParams.poolOwners", source = "model.owners")
  @Mapping(target = "metadata.poolRegistrationParams.marginPercentage", source = "model.margin")
  @Mapping(target = "metadata.poolRegistrationParams.relays", source = "model.relays")
  @Mapping(target = "metadata.poolRegistrationParams.vrfKeyHash", source = "model.vrfKeyHash")
  @Mapping(target = "metadata.poolRegistrationParams.rewardAddress", source = "model.rewardAccount")
  Operation mapPoolRegistrationToOperation(PoolRegistration model, OperationStatus status, int index);

  PoolRetirement mapEntityToPoolRetirement(PoolRetirementEntity entity);

  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_POOL_RETIREMENT)
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.poolId")
  @Mapping(target = "metadata.epoch", source = "model.epoch")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  Operation mapPoolRetirementToOperation(PoolRetirement model, OperationStatus status, int index);

  @Mapping(source = "txHash", target = "txHash")
  @Mapping(source = "certIndex", target = "certIndex")
  @Mapping(source = "address", target = "address")
  @Mapping(source = "drepHash", target = "drep.drepId") // Map entity's drepHash (hex) to domain's drepId field (Rosetta uses hex, not human readable bech32)
  @Mapping(source = "drepType", target = "drep.drepType", qualifiedByName = "convertYaciDrepType")
  DRepDelegation mapEntityToDRepDelegation(DrepVoteDelegationEntity entity);

  @Mapping(source = "txHash", target = "txHash")
  @Mapping(source = "certIndex", target = "certIndex")
  @Mapping(source = "address", target = "address")
  @Mapping(source = "drep.drepId", target = "drepHash") // Map entity's drepHash (hex) to domain's drepId field (Rosetta uses hex, not human readable bech32 format)
  @Mapping(source = "drep.drepType", target = "drepType", qualifiedByName = "convertClientDrepType")
  DrepVoteDelegationEntity mapDRepDelegationToEntity(DRepDelegation dRepDelegation);

  StakePoolDelegation mapPoolDelegationEntityToDelegation(PoolDelegationEntity entity);

  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_STAKE_DELEGATION)
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "account.address", source = "model.address")
  @Mapping(target = "metadata.poolKeyHash", source = "model.poolId")
  Operation mapStakeDelegationToOperation(StakePoolDelegation model, OperationStatus status, int index);

  /**
   * Maps a VotingProcedureEntity to a GovernancePoolVote domain object.
   * Only applies to SPO votes (voter_type = STAKING_POOL_KEY_HASH).
   *
   * @param entity the voting procedure entity from the database
   * @return the governance pool vote domain object
   */
  @Nullable
  default GovernancePoolVote mapVotingProcedureEntityToGovernancePoolVote(@Nullable VotingProcedureEntity entity) {
    if (entity == null) {
      return null;
    }

    // Convert Vote enum to cardano-client Vote enum
    com.bloxbean.cardano.client.transaction.spec.governance.Vote vote = switch (entity.getVote()) {
      case YES -> com.bloxbean.cardano.client.transaction.spec.governance.Vote.YES;
      case NO -> com.bloxbean.cardano.client.transaction.spec.governance.Vote.NO;
      case ABSTAIN -> com.bloxbean.cardano.client.transaction.spec.governance.Vote.ABSTAIN;
    };

    // Build GovActionId from gov_action_tx_hash and gov_action_index
    com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId govActionId =
        new com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId(
            entity.getGovActionTxHash(),
            entity.getGovActionIndex()
        );

    // Build optional Anchor if anchor data exists
    com.bloxbean.cardano.client.transaction.spec.governance.Anchor voteRationale = null;
    if (entity.getAnchorUrl() != null && entity.getAnchorHash() != null) {
      voteRationale = com.bloxbean.cardano.client.transaction.spec.governance.Anchor.builder()
          .anchorUrl(entity.getAnchorUrl())
          .anchorDataHash(com.bloxbean.cardano.client.util.HexUtil.decodeHexString(entity.getAnchorHash()))
          .build();
    }

    // Build Voter with STAKING_POOL_KEY_HASH type
    com.bloxbean.cardano.client.address.Credential credential =
        com.bloxbean.cardano.client.address.Credential.fromKey(
            com.bloxbean.cardano.client.util.HexUtil.decodeHexString(entity.getVoterHash())
        );
    com.bloxbean.cardano.client.transaction.spec.governance.Voter voter =
        new com.bloxbean.cardano.client.transaction.spec.governance.Voter(
            com.bloxbean.cardano.client.transaction.spec.governance.VoterType.STAKING_POOL_KEY_HASH,
            credential
        );

    return GovernancePoolVote.builder()
        .govActionId(govActionId)
        .poolCredentialHex(entity.getVoterHash())
        .vote(vote)
        .voter(voter)
        .voteRationale(voteRationale)
        .build();
  }

  @Named("convertYaciDrepType")
  default com.bloxbean.cardano.client.transaction.spec.governance.DRepType convertYaciDrepType(
      com.bloxbean.cardano.yaci.core.model.governance.DrepType yaciDrepType) {
    if (yaciDrepType == null) {
      return null;
    }
    return switch (yaciDrepType) {
      case ADDR_KEYHASH -> com.bloxbean.cardano.client.transaction.spec.governance.DRepType.ADDR_KEYHASH;
      case SCRIPTHASH -> com.bloxbean.cardano.client.transaction.spec.governance.DRepType.SCRIPTHASH;
      case ABSTAIN -> com.bloxbean.cardano.client.transaction.spec.governance.DRepType.ABSTAIN;
      case NO_CONFIDENCE -> com.bloxbean.cardano.client.transaction.spec.governance.DRepType.NO_CONFIDENCE;
    };
  }

  @Named("convertClientDrepType")
  default com.bloxbean.cardano.yaci.core.model.governance.DrepType convertClientDrepType(
      com.bloxbean.cardano.client.transaction.spec.governance.DRepType clientDrepType) {
    if (clientDrepType == null) {
      return null;
    }
    return switch (clientDrepType) {
      case ADDR_KEYHASH -> com.bloxbean.cardano.yaci.core.model.governance.DrepType.ADDR_KEYHASH;
      case SCRIPTHASH -> com.bloxbean.cardano.yaci.core.model.governance.DrepType.SCRIPTHASH;
      case ABSTAIN -> com.bloxbean.cardano.yaci.core.model.governance.DrepType.ABSTAIN;
      case NO_CONFIDENCE -> com.bloxbean.cardano.yaci.core.model.governance.DrepType.NO_CONFIDENCE;
    };
  }

  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_DREP_VOTE_DELEGATION)
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "account.address", source = "model.address")
  @Mapping(target = "metadata.drep", source = "model.drep", qualifiedByName = "convertDRepFromRosetta")
  Operation mapDRepDelegationToOperation(DRepDelegation model, OperationStatus status, int index);

  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_POOL_GOVERNANCE_VOTE)
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "metadata.poolGovernanceVoteParams.governanceActionHash", source = "governancePoolVote.govActionId", qualifiedByName = "convertGovActionIdToRosetta")
  @Mapping(target = "metadata.poolGovernanceVoteParams.poolCredential", source = "governancePoolVote.poolCredentialHex", qualifiedByName = "convertPoolCredentialToRosetta")
  @Mapping(target = "metadata.poolGovernanceVoteParams.vote", source = "governancePoolVote.vote", qualifiedByName = "convertGovVoteToRosetta")
  @Mapping(target = "metadata.poolGovernanceVoteParams.voteRationale", source = "governancePoolVote.voteRationale", qualifiedByName = "convertGovAnchorFromRosetta")
  Operation mapGovernanceVoteToOperation(GovernancePoolVote governancePoolVote, OperationStatus status, int index);

  @Mapping(target = "type", constant = Constants.INPUT)
  @Mapping(target = "coinChange.coinAction", source = "model", qualifiedByName = "getCoinSpentAction")
  @Mapping(target = "metadata", source = "model.amounts", qualifiedByName = "mapAmountsToOperationMetadataInputWithCache")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "amount.value", source = "model", qualifiedByName = "getAdaAmountInput")
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.ownerAddr")
  @Mapping(target = "amount.currency.symbol", constant = Constants.ADA)
  @Mapping(target = "amount.currency.decimals", constant = Constants.ADA_DECIMALS_STRING)
  @Mapping(target = "coinChange.coinIdentifier.identifier", source = "model", qualifiedByName = "getUtxoName")
  Operation mapInputUtxoToOperation(Utxo model, OperationStatus status, int index, @Context Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap);

  @Mapping(target = "type", constant = Constants.OUTPUT)
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "coinChange.coinAction", source = "model", qualifiedByName = "getCoinCreatedAction")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "metadata", source = "model.amounts", qualifiedByName = "mapAmountsToOperationMetadataOutputWithCache")
  @Mapping(target = "account.address", source = "model.ownerAddr")
  @Mapping(target = "amount.value", source = "model", qualifiedByName = "getAdaAmountOutput")
  @Mapping(target = "amount.currency.symbol", constant = Constants.ADA)
  @Mapping(target = "amount.currency.decimals", constant = Constants.ADA_DECIMALS_STRING)
  @Mapping(target = "coinChange.coinIdentifier.identifier", source = "model", qualifiedByName = "getUtxoName")
  Operation mapOutputUtxoToOperation(Utxo model, OperationStatus status, int index, @Context Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap);

  StakeRegistration mapStakeRegistrationEntityToStakeRegistration(StakeRegistrationEntity entity);

  @Mapping(target = "type", source = "model.type", qualifiedByName = "convertStakeCertificateType")
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.address")
  @Mapping(target = "metadata.depositAmount", source = "model", qualifiedByName = "getDepositAmountStake", conditionExpression = "java(isRegistration(stakeRegistration.getType()))")
  @Mapping(target = "metadata.refundAmount", source = "model", qualifiedByName = "getDepositAmountStake", conditionExpression = "java(!isRegistration(stakeRegistration.getType()))")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  Operation mapStakeRegistrationToOperation(StakeRegistration model, OperationStatus status, int index);

  @Named("isRegistration")
  default boolean isRegistration(CertificateType type) {
    return type == CertificateType.STAKE_REGISTRATION;
  }

  @Mapping(target = "stakeAddress", source = "address")
  Withdrawal mapWithdrawalEntityToWithdrawal(WithdrawalEntity model);

  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_WITHDRAWAL)
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.stakeAddress")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "metadata.withdrawalAmount", source = "model.amount", qualifiedByName = "updateDepositAmountNegate")
  @Mapping(target = "amount", ignore = true)
  Operation mapWithdrawalToOperation(Withdrawal model, OperationStatus status, int index);

}
