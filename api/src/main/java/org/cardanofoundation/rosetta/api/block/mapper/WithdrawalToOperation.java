package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.Withdrawal;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

@Mapper(config = BaseMapper.class, uses = {OperationMapperUtils.class})
public interface WithdrawalToOperation {

  @Mapping(target = "type", constant = Constants.OPERATION_TYPE_WITHDRAWAL)
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.stakeAddress")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "metadata.withdrawalAmount", source = "model.amount", qualifiedByName = "updateDepositAmountNegate")
  @Mapping(target = "amount", ignore = true)
  Operation toDto(Withdrawal model, OperationStatus status, int index);

}
