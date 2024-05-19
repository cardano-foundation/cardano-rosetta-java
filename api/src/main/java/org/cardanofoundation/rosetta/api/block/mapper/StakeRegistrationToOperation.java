package org.cardanofoundation.rosetta.api.block.mapper;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.cardanofoundation.rosetta.common.mapper.OperationMapperUtils;

@Mapper(config = BaseMapper.class, uses = {OperationMapperUtils.class})
public interface StakeRegistrationToOperation {

  @Mapping(target = "type", source = "model.type", qualifiedByName = "convertCertificateType")
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.address")
  @Mapping(target = "metadata.depositAmount", source = "model", qualifiedByName = "getDepositAmountStake", conditionExpression = "java(isRegistration(stakeRegistration.getType()))")
  @Mapping(target = "metadata.refundAmount", source = "model", qualifiedByName = "getDepositAmountStake", conditionExpression = "java(!isRegistration(stakeRegistration.getType()))")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  abstract Operation toDto(StakeRegistration model, OperationStatus status, int index);

  @Named("isRegistration")
  default boolean isRegistration(CertificateType type) {
    return type == CertificateType.STAKE_REGISTRATION;
  }

}
