package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.openapitools.client.model.Amount;
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
    return Optional
        .ofNullable(modelMapper.getTypeMap(Delegation.class, Operation.class))
        .orElseGet(() -> modelMapper.createTypeMap(Delegation.class, Operation.class))
        .addMappings(mp -> {
          mp.map(f -> status.getStatus(), Operation::setStatus);
          mp.map(f-> OperationType.STAKE_DELEGATION.getValue(), Operation::setType);
          mp.<Long>map(f -> index, (d,v) -> d.getOperationIdentifier().setIndex(v));
          mp.<Amount>map(f->getDepositAmount("2000000"), (d, v) -> d.getMetadata().setDepositAmount(v));
          mp.<String>map(Delegation::getAddress, (d, v) -> d.getAccount().setAddress(v));

        })
        .setPostConverter(MappingContext::getDestination)
        .map(model);
  }

}
