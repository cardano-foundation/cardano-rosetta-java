package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.PoolRegistrationParams;

import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;

@OpenApiMapper
@AllArgsConstructor
public class PoolRegistrationToOperation extends AbstractToOperation<PoolRegistration> {

  final ModelMapper modelMapper;

  @Override
  public Operation toDto(PoolRegistration model, OperationStatus status, int index) {
    return modelMapper.typeMap(PoolRegistration.class, Operation.class)
        .addMappings(mp -> {
          mp.skip(Operation::setMetadata);
          mp.<Long>map(f -> index, (d, v) -> d.getOperationIdentifier().setIndex(v));
          mp.map(f -> status.getStatus(), Operation::setStatus);
          mp.map(f -> OperationType.POOL_REGISTRATION.getValue(), Operation::setType);
          mp.<String>map(PoolRegistration::getPoolId, (d, v) -> d.getAccount().setAddress(v));
        })
        .setPostConverter(ctx -> {
          var d = ctx.getDestination();
          d.setMetadata(new OperationMetadata());

          ctx.getDestination().getMetadata().setDepositAmount(getDepositAmount());
          ctx.getDestination().getMetadata()
              .setPoolRegistrationParams(new PoolRegistrationParams());
          var params = ctx.getDestination().getMetadata().getPoolRegistrationParams();
          params.setPledge(ctx.getSource().getPledge());
          params.setCost(ctx.getSource().getCost());
          params.setPoolOwners(ctx.getSource().getOwners().stream().toList());
          params.setMarginPercentage(ctx.getSource().getMargin());
          params.setRelays(ctx.getSource().getRelays());
          params.setVrfKeyHash(ctx.getSource().getVrfKeyHash());
          params.setRewardAddress(ctx.getSource().getRewardAccount());
          return d;
        })
        .map(model);
  }


}
