package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.Optional;

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
public class InputToOperation extends AbstractToOperation<Utxo> {

  private static final String INPUT_OP_MAP = "inputOpMap";
  final ModelMapper modelMapper;

  @Override
  public Operation toDto(Utxo model, OperationStatus status, int index) {
    return Optional
        .ofNullable(modelMapper.getTypeMap(Utxo.class, Operation.class, INPUT_OP_MAP))
        .orElseGet(() -> modelMapper.createTypeMap(Utxo.class, Operation.class,INPUT_OP_MAP))
        .addMappings(mp -> {
          mp.map(f -> Constants.INPUT, Operation::setType);
          mp.<CoinAction>map(f -> CoinAction.SPENT, (d, v) -> d.getCoinChange().setCoinAction(v));
          mp.map(f-> mapToOperationMetaData(true, model.getAmounts()), Operation::setMetadata);
          mapOthers(model, status, index, mp, true);
        })
        .map(model);
  }


}
