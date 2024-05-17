package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
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
//        .addMappings(mp -> {
//          mp.map(f -> status.getStatus(), Operation::setStatus);
//          mp.map(f-> OperationType.STAKE_DELEGATION.getValue(), Operation::setType);
//          mp.<Long>map(f -> index, (d,v) -> d.getOperationIdentifier().setIndex(v));
//          mp.<String>map(Delegation::getAddress, (d, v) -> d.getAccount().setAddress(v));
//          mp.<String>map(Delegation::getPoolId, (d, v) -> d.getMetadata().setPoolKeyHash(v));
//        })
        .setPostConverter(ctx -> {
          var d = ctx.getDestination();
          d.setMetadata(new OperationMetadata());
          d.setAccount(new AccountIdentifier());
          d.setOperationIdentifier(new OperationIdentifier());

          d.getAccount().setAddress(ctx.getSource().getAddress());
          d.setType(OperationType.STAKE_DELEGATION.getValue());
          d.setStatus(status.getStatus());
          d.getOperationIdentifier().setIndex((long)index);

          ctx.getDestination().getMetadata().setPoolKeyHash(model.getPoolId());
          return d;
        })
        .map(model);
  }

}
