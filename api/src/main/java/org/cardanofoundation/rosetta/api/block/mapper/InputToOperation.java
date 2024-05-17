package org.cardanofoundation.rosetta.api.block.mapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.openapitools.client.model.CoinAction;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.mapper.UtxoMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

@Component
@RequiredArgsConstructor
public class InputToOperation extends AbstractToOperation<Utxo> {

  private final UtxoMapper utxoMapper;

  @Override
  public Operation toDto(Utxo model, OperationStatus status, int index) {
    Operation operation = utxoMapper.mapToOperation(model, status, index, true);
    operation.setType(Constants.INPUT);
    operation.getCoinChange().setCoinAction(CoinAction.SPENT);
    operation.setMetadata(mapToOperationMetaData(true, model.getAmounts()));
    return operation;
  }
}


