package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
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
