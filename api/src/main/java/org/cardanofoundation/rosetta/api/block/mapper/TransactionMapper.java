package org.cardanofoundation.rosetta.api.block.mapper;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
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

  StakePoolDelegation mapDelegationEntityToDelegation(DelegationEntity entity);

  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_STAKE_DELEGATION)
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "account.address", source = "model.address")
  @Mapping(target = "metadata.poolKeyHash", source = "model.poolId")
  Operation mapStakeDelegationToOperation(StakePoolDelegation model, OperationStatus status, int index);

  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_DREP_VOTE_DELEGATION)
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "account.address", source = "model.address")
  @Mapping(target = "metadata.drep", source = "model.drep", qualifiedByName = "convertDRepFromRosetta")
  Operation mapDRepDelegationToOperation(DRepDelegation model, OperationStatus status, int index);

  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_POOL_GOVERNANCE_VOTE)
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "metadata.poolGovernanceVoteParams.governanceAction", source = "governancePoolVote.govActionId", qualifiedByName = "convertGovActionIdToRosetta")
  @Mapping(target = "metadata.poolGovernanceVoteParams.poolCredential", source = "governancePoolVote.poolCredentialHex", qualifiedByName = "convertPoolCredentialToRosetta")
  @Mapping(target = "metadata.poolGovernanceVoteParams.vote", source = "governancePoolVote.vote", qualifiedByName = "convertGovVoteToRosetta")
  @Mapping(target = "metadata.poolGovernanceVoteParams.voteRationale", source = "governancePoolVote.voteRationale", qualifiedByName = "convertGovAnchorFromRosetta")
  Operation mapGovernanceVoteToOperation(GovernancePoolVote governancePoolVote, OperationStatus status, int index);

  @Mapping(target = "type", constant = Constants.INPUT)
  @Mapping(target = "coinChange.coinAction", source = "model", qualifiedByName = "getCoinSpentAction")
  @Mapping(target = "metadata", source = "model.amounts", qualifiedByName = "mapAmountsToOperationMetadataInput")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "amount.value", source = "model", qualifiedByName = "getAdaAmountInput")
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.ownerAddr")
  @Mapping(target = "amount.currency.symbol", constant = Constants.ADA)
  @Mapping(target = "amount.currency.decimals", constant = Constants.ADA_DECIMALS_STRING)
  @Mapping(target = "coinChange.coinIdentifier.identifier", source = "model", qualifiedByName = "getUtxoName")
  Operation mapInputUtxoToOperation(Utxo model, OperationStatus status, int index);

  @Mapping(target = "type", constant = Constants.OUTPUT)
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "coinChange.coinAction", source = "model", qualifiedByName = "getCoinCreatedAction")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "metadata", source = "model.amounts", qualifiedByName = "mapAmountsToOperationMetadataOutput")
  @Mapping(target = "account.address", source = "model.ownerAddr")
  @Mapping(target = "amount.value", source = "model", qualifiedByName = "getAdaAmountOutput")
  @Mapping(target = "amount.currency.symbol", constant = Constants.ADA)
  @Mapping(target = "amount.currency.decimals", constant = Constants.ADA_DECIMALS_STRING)
  @Mapping(target = "coinChange.coinIdentifier.identifier", source = "model", qualifiedByName = "getUtxoName")
  Operation mapOutputUtxoToOperation(Utxo model, OperationStatus status, int index);

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
