package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;

import lombok.AllArgsConstructor;

import com.bloxbean.cardano.yaci.core.model.certs.CertificateType;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.cardanofoundation.rosetta.common.util.Constants.ADA;

@OpenApiMapper
@AllArgsConstructor
public class InputToOperation extends AbstractToOperation<Utxo> {

  final ModelMapper modelMapper;

  @Override
  public Operation toDto(Utxo model, OperationStatus status, int index) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Utxo.class, Operation.class))
        .orElseGet(() -> modelMapper.createTypeMap(Utxo.class, Operation.class))
        .addMappings(mp -> {

          mp.map(f -> Constants.INPUT, Operation::setType);
          mp.map(f -> status.getStatus(), Operation::setStatus);
          mp.<String>map(Utxo::getOwnerAddr, (d, v) -> d.getAccount().setAddress(v));
          mp.map(Utxo::getLovelaceAmount, (d, v) -> d.getAmount().setValue(String.valueOf(v)));
          mp.<String>map(f -> ADA, (d, v) -> d.getAmount().getCurrency().setSymbol(v));
          mp.map(f -> f,
              (d, v) -> d.getCoinChange()
                  .getCoinIdentifier()
                  .setIdentifier(model.getTxHash() + ":" + model.getOutputIndex()));
          mp.<CoinAction>map(f -> CoinAction.SPENT, (d, v) -> d.getCoinChange().setCoinAction(v));
          mp.map(f-> mapToOperationMetaData(true, model.getAmounts()), Operation::setMetadata); //TODO saa: test it!

        })
        .setPostConverter(MappingContext::getDestination)
        .map(model);
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
