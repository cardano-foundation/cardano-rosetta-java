package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;

@OpenApiMapper
@AllArgsConstructor
public class DelegationToOperation extends AbstractToOperation<Delegation> {

  final ModelMapper modelMapper;

  @Override
  public Operation toDto(Delegation model, OperationStatus status, int index) {
    return modelMapper.typeMap(Delegation.class, Operation.class)
        .addMappings(mp -> {
          mp.map(f -> status.getStatus(), Operation::setStatus);
          mp.map(f-> OperationType.STAKE_DELEGATION.getValue(), Operation::setType);
          mp.<Long>map(f -> index, (d,v) -> d.getOperationIdentifier().setIndex(v));
          mp.<String>map(Delegation::getAddress, (d, v) -> d.getAccount().setAddress(v));
          mp.<String>map(Delegation::getPoolId, (d, v) -> d.getMetadata().setPoolKeyHash(v));
        })
        .setPostConverter(MappingContext::getDestination)
        .map(model);
  }

}
