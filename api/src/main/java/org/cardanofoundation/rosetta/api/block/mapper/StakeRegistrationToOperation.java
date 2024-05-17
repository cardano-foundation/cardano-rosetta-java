package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationIdentifier;
import org.openapitools.client.model.OperationMetadata;
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
        .setPostConverter(ctx -> {
          var d = ctx.getDestination();
          d.setMetadata(new OperationMetadata());
          d.setAccount(new AccountIdentifier());
          d.setOperationIdentifier(new OperationIdentifier());

          d.getAccount().setAddress(ctx.getSource().getAddress());
          d.setType(convert(ctx.getSource().getType()));
          d.setStatus(status.getStatus());
          d.getOperationIdentifier().setIndex((long)index);

          ctx.getDestination().getMetadata().setDepositAmount(getDepositAmount());
          return d;

        })
        .map(model);
  }


  private String convert(CertificateType model) {
    if (model == null) {
      return null;
    } else {
      return switch (model) {
        case CertificateType.STAKE_REGISTRATION -> OperationType.STAKE_KEY_REGISTRATION.getValue();
        case CertificateType.STAKE_DEREGISTRATION -> OperationType.STAKE_KEY_DEREGISTRATION.getValue();
        default -> null;
      };
    }
  }


}
