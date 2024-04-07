package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;

import static org.cardanofoundation.rosetta.common.util.Constants.ADA;
import static org.cardanofoundation.rosetta.common.util.Constants.ADA_DECIMALS;

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

          mp.map(f-> OperationType.STAKE_DELEGATION.getValue(), Operation::setType);
          mp.<Long>map(f -> index, (d,v) -> d.getOperationIdentifier().setIndex(v));
          mp.<String>map(Delegation::getAddress, (d, v) -> d.getAccount().setAddress(v));
          mp.<String>map(Delegation::getPoolId, (d, v) -> d.getMetadata().setPoolKeyHash(v));

        })
        .setPostConverter(ctx -> {

          ctx.getDestination().setStatus(status.getStatus());
          ctx.getDestination().setMetadata(OperationMetadata.builder()
              .depositAmount(DataMapper.mapAmount("2000000", ADA, ADA_DECIMALS, null))
              .build());
          // TODO saa: need to get this from protocolparams
          // Create and inject  GenesisService to get the stake deposit amount
          // see similar implementation in BlockService.getPoolDeposit

          return ctx.getDestination();
        }).map(model);
  }

}
