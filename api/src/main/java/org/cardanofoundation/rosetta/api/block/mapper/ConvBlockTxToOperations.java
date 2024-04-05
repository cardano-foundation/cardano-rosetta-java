package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;

@OpenApiMapper
@AllArgsConstructor
class ConvBlockTxToOperations  {

  private final StakeRegistrationToOperation stakeToOperation;


  protected List<Operation> convert(List<StakeRegistration> stakeReg, OperationStatus status) {
    return Optional.ofNullable(stakeReg)
        .stream()
        .flatMap(List::stream)
        .map(t -> stakeToOperation.toDto(t, status))
        .collect(Collectors.toList());
  }
}
