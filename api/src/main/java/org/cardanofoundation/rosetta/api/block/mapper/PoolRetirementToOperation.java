package org.cardanofoundation.rosetta.api.block.mapper;

import org.cardanofoundation.rosetta.common.mapper.OperationMapperUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

@Mapper(config = BaseMapper.class, uses = {OperationMapperUtils.class})
public interface PoolRetirementToOperation {

  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_POOL_RETIREMENT)
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.poolId")
  @Mapping(target = "metadata.epoch", source = "model.epoch")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  Operation toDto(PoolRetirement model, OperationStatus status, int index);

}
