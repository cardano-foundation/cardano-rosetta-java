package org.cardanofoundation.rosetta.api.block.mapper;

import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;

@Mapper(config = BaseMapper.class, uses = {OperationMapperUtils.class})
public interface StakeRegistrationToOperation {

  @Mapping(target = "type", source = "model.type", qualifiedByName = "convertCertificateType")
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.address")
  @Mapping(target = "metadata.depositAmount", expression = "java(operationMapperUtils.getDepositAmount())")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  abstract Operation toDto(StakeRegistration model, OperationStatus status, int index);


}
