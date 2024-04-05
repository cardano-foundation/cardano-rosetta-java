package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;

public abstract class  AbstractToOperation<T> {

  abstract Operation toDto(T model, OperationStatus status);

  protected List<Operation> convert(List<T> stakeReg, OperationStatus status) {
    return Optional.ofNullable(stakeReg)
        .stream()
        .flatMap(List::stream)
        .map(t -> toDto(t, status))
        .collect(Collectors.toList());
  }

}
