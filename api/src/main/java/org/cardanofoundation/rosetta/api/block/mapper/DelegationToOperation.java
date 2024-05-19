package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.cardanofoundation.rosetta.common.mapper.OperationMapperUtils;
import org.cardanofoundation.rosetta.common.util.Constants;

@Mapper(config = BaseMapper.class, uses = {OperationMapperUtils.class})
public interface DelegationToOperation {

  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_STAKE_DELEGATION)
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "account.address", source = "model.address")
  @Mapping(target = "metadata.poolKeyHash", source = "model.poolId")
  Operation toDto(Delegation model, OperationStatus status, int index);

}
