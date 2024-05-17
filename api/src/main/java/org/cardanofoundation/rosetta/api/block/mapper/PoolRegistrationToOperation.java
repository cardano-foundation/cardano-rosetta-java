package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
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
        .setPostConverter(ctx -> {
          var d = ctx.getDestination();
          d.setMetadata(new OperationMetadata());
          d.setAccount(new AccountIdentifier());
          d.setOperationIdentifier(new OperationIdentifier());

          d.getAccount().setAddress(ctx.getSource().getPoolId());
          d.setType(OperationType.POOL_REGISTRATION.getValue());
          d.setStatus(status.getStatus());
          d.getOperationIdentifier().setIndex((long)index);

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
