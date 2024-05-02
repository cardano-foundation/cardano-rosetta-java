package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;

@OpenApiMapper
@AllArgsConstructor
public class StakeRegistrationToOperation extends AbstractToOperation<StakeRegistration> {

  final ModelMapper modelMapper;
  @Override
  public Operation toDto(StakeRegistration model, OperationStatus status, int index) {
    return modelMapper.typeMap(StakeRegistration.class, Operation.class)
        .addMappings(mp -> {

          mp.map(f -> status.getStatus(), Operation::setStatus);
          mp.map(f -> convert(model.getType()), Operation::setType);
          mp.<String>map(StakeRegistration::getAddress, (d, v) -> d.getAccount().setAddress(v));
          mp.<Amount>map(f -> getDepositAmount(), (d, v) -> d.getMetadata().setDepositAmount(v));
          mp.<Long>map(f -> index, (d, v) -> d.getOperationIdentifier().setIndex(v));


        })
        .setPostConverter(MappingContext::getDestination)
        .map(model);
  }


  private String convert(CertificateType model) {
    if (model == null) {
      return null;
    } else {
      return model.equals(CertificateType.STAKE_REGISTRATION)
          ? OperationType.STAKE_KEY_REGISTRATION.getValue() :
          model.equals(CertificateType.STAKE_DEREGISTRATION)
              ? OperationType.STAKE_KEY_DEREGISTRATION.getValue() : null;
    }
  }


}
