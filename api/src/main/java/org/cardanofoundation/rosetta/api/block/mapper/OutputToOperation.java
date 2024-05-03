package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.annotation.OpenApiMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

@OpenApiMapper
@AllArgsConstructor
public class OutputToOperation extends AbstractToOperation<Utxo> {

  final ModelMapper modelMapper;

  @Override
  public Operation toDto(Utxo model, OperationStatus status, int index) {
    return modelMapper.typeMap(Utxo.class, Operation.class)
        .addMappings(mp -> {
          mp.map(f -> Constants.OUTPUT, Operation::setType);
          mp.<CoinAction>map(f -> CoinAction.CREATED, (d, v) -> d.getCoinChange().setCoinAction(v));
          mp.<Long>map(f -> model.getOutputIndex(), (d, v) -> d.getOperationIdentifier()
              .setNetworkIndex(v));
          mp.map(f-> mapToOperationMetaData(false, model.getAmounts()), Operation::setMetadata);
          mapOthers(model, status, index, mp, false);
        })
        .map(model);
  }
}
