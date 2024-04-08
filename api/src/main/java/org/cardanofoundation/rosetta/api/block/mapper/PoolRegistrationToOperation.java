package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.Amount;
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
    return Optional
        .ofNullable(modelMapper.getTypeMap(PoolRegistration.class, Operation.class))
        .orElseGet(() -> modelMapper.createTypeMap(PoolRegistration.class, Operation.class))
        .addMappings(mp -> {
          mp.skip(Operation::setMetadata);
        })
//        .implicitMappings()
//        .addMappings(mp ->{
//          mp.<Amount>map(f -> getDepositAmount("5000"),
//              (d, v) -> d.getMetadata().setDepositAmount(v));
////          mp.<String>map(PoolRegistration::getPledge,
////              (d, v) -> d.getMetadata().getPoolRegistrationParams().setPledge(v));
////          mp.<String>map(PoolRegistration::getCost,
////              (d, v) -> d.getMetadata().getPoolRegistrationParams().setCost(v));
////          mp.<List<String>>map(f->model.getOwners().stream().toList(),
////              (d, v) -> d.getMetadata().getPoolRegistrationParams().setPoolOwners(v));
////          mp.<String>map(PoolRegistration::getMargin,
////              (d, v) -> d.getMetadata().getPoolRegistrationParams().setMarginPercentage(v));
////          mp.<List<Relay>>map(PoolRegistration::getRelays,
////              (d, v) -> d.getMetadata().getPoolRegistrationParams().setRelays(v));
//
//        })
        .addMappings(mp -> {
          mp.skip(Operation::setMetadata);

          mp.<Long>map(f -> index, (d, v) -> d.getOperationIdentifier().setIndex(v));
          mp.map(f -> status.getStatus(), Operation::setStatus);
          mp.map(f -> OperationType.POOL_REGISTRATION.getValue(), Operation::setType);
          mp.<String>map(PoolRegistration::getPoolId, (d, v) -> d.getAccount().setAddress(v));


        })

        .setPostConverter(ctx -> {
          Operation d = ctx.getDestination();
          d.setMetadata(new OperationMetadata());

          ctx.getDestination().getMetadata().setDepositAmount(getDepositAmount("5000"));
          ctx.getDestination().getMetadata().setPoolRegistrationParams(new PoolRegistrationParams());
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
