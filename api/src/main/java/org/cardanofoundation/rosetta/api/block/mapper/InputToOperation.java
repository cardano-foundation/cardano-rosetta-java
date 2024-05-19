package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.OperationMapperUtils;

@Mapper(config = BaseMapper.class, uses = {OperationMapperUtils.class})
public interface InputToOperation {

  @Mapping(target = "type", constant = Constants.INPUT)
  @Mapping(target = "coinChange.coinAction", source = "model", qualifiedByName = "getCoinSpentAction")
  @Mapping(target = "metadata", source = "model.amounts", qualifiedByName = "mapAmountsToOperationMetadataInput")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "amount.value", source = "model", qualifiedByName = "getAdaAmountInput")
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "account.address", source = "model.ownerAddr")
  @Mapping(target = "amount.currency.symbol", constant = Constants.ADA)
  @Mapping(target = "amount.currency.decimals", constant = Constants.ADA_DECIMALS_STRING)
  @Mapping(target = "coinChange.coinIdentifier.identifier", source = "model", qualifiedByName = "getUtxoName")
  Operation toDto(Utxo model, OperationStatus status, int index);

}
