package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.OperationMapperUtils;

@Mapper(config = BaseMapper.class, uses = {OperationMapperUtils.class})
public interface PoolRegistrationToOperation {

  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_POOL_REGISTRATION)
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.poolId")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "metadata.depositAmount", expression = "java(operationMapperUtils.getDepositAmountPool())")
  @Mapping(target = "metadata.poolRegistrationParams.pledge", source = "model.pledge")
  @Mapping(target = "metadata.poolRegistrationParams.cost", source = "model.cost")
  @Mapping(target = "metadata.poolRegistrationParams.poolOwners", source = "model.owners")
  @Mapping(target = "metadata.poolRegistrationParams.marginPercentage", source = "model.margin")
  @Mapping(target = "metadata.poolRegistrationParams.relays", source = "model.relays")
  @Mapping(target = "metadata.poolRegistrationParams.vrfKeyHash", source = "model.vrfKeyHash")
  @Mapping(target = "metadata.poolRegistrationParams.rewardAddress", source = "model.rewardAccount")
  Operation toDto(PoolRegistration model, OperationStatus status, int index);

}
