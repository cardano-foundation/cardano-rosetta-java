package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.cardanofoundation.rosetta.common.util.Constants.ADA;
import static org.cardanofoundation.rosetta.common.util.Constants.ADA_DECIMALS;

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
          mp.<Long>map(f -> index, (d, v) -> d.getOperationIdentifier().setIndex(v));
          mp.<String>map(Utxo::getOwnerAddr, (d, v) -> d.getAccount().setAddress(v));
          mp.map(Utxo::getLovelaceAmount, (d, v) -> d.getAmount().setValue(String.valueOf(v)));
          mp.<String>map(f -> ADA, (d, v) -> d.getAmount().getCurrency().setSymbol(v));
          mp.<Integer>map(f -> ADA_DECIMALS, (d, v) -> d.getAmount().getCurrency().setDecimals(v));
          mp.<String>map(f -> model.getTxHash() + ":" + model.getOutputIndex(),
              (d, v) -> d.getCoinChange().getCoinIdentifier().setIdentifier(v));
          mp.<CoinAction>map(f -> CoinAction.SPENT, (d, v) -> d.getCoinChange().setCoinAction(v));
          mp.map(f-> mapToOperationMetaData(true, model.getAmounts()), Operation::setMetadata);

        })
        //TODO saa: strange but without it method mapToOperationMetaData in above mapper did not work
        .setPostConverter(MappingContext::getDestination)
        .map(model);
  }
}
