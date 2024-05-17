package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;

@OpenApiMapper
@AllArgsConstructor
public class PoolRetirementToOperation extends AbstractToOperation<PoolRetirement> {

  final ModelMapper modelMapper;

  @Override
  public Operation toDto(PoolRetirement model, OperationStatus status, int index) {
    return modelMapper.typeMap(PoolRetirement.class, Operation.class)
//        .addMappings(mp -> {
//          mp.map(f -> status.getStatus(), Operation::setStatus);
//          mp.map(f -> OperationType.POOL_RETIREMENT.getValue(), Operation::setType);
//          mp.<String>map(PoolRetirement::getPoolId, (d, v) -> d.getAccount().setAddress(v));
//          mp.map(f -> OperationMetadata.builder().epoch(model.getEpoch()).build(),
//              Operation::setMetadata);
//          mp.<Long>map(f -> index, (d, v) -> d.getOperationIdentifier().setIndex(v));
//        })
        .setPostConverter(ctx -> {
          var d = ctx.getDestination();
          d.setAccount(new AccountIdentifier());
          d.setOperationIdentifier(new OperationIdentifier());
          d.setMetadata(new OperationMetadata());

          d.getAccount().setAddress(ctx.getSource().getPoolId());
          d.setType(OperationType.POOL_RETIREMENT.getValue());
          d.setStatus(status.getStatus());
          d.getOperationIdentifier().setIndex((long)index);

          ctx.getDestination().getMetadata().setEpoch(model.getEpoch());
          return d;
        })
        .map(model);
  }
}
