package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;

import lombok.AllArgsConstructor;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.modelmapper.ModelMapper;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;

import static org.cardanofoundation.rosetta.common.util.Constants.ADA;
import static org.cardanofoundation.rosetta.common.util.Constants.ADA_DECIMALS;

@OpenApiMapper
@AllArgsConstructor
public class StakeRegistrationToOperation extends AbstractToOperation<StakeRegistration> {

  final ModelMapper modelMapper;

  @Override
  public Operation toDto(StakeRegistration model, OperationStatus status, int index) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(StakeRegistration.class, Operation.class))
        .orElseGet(() -> modelMapper.createTypeMap(StakeRegistration.class, Operation.class))
        .addMappings(mp -> {

          mp.<CertificateType>map(StakeRegistration::getType, (d, v) -> d.setType(convert(v)));
          mp.<String>map(StakeRegistration::getAddress, (d, v) -> d.getAccount().setAddress(v));

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


  private String convert(CertificateType model) {
    if (model == null) {
      return null;
    } else {
      return model.equals(CertificateType.STAKE_REGISTRATION)
          ? OperationType.STAKE_KEY_REGISTRATION.toString() :
          model.equals(CertificateType.STAKE_DEREGISTRATION)
              ? OperationType.STAKE_KEY_DEREGISTRATION.toString() : null;
    }
  }


}
