package org.cardanofoundation.rosetta.api.block.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.mapper.BaseMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

@Mapper(config = BaseMapper.class, uses = {OperationMapperUtils.class})
public interface OutputToOperation {

  @Mapping(target = "type", constant = Constants.OUTPUT)
  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "coinChange.coinAction", source = "model", qualifiedByName = "getCoinCreatedAction")
  @Mapping(target = "operationIdentifier", source = "index", qualifiedByName = "OperationIdentifier")
  @Mapping(target = "metadata", source = "model.amounts", qualifiedByName = "mapAmountsToOperationMetadataOutput")
  @Mapping(target = "account.address", source = "model.ownerAddr")
  @Mapping(target = "amount.value", source = "model", qualifiedByName = "getAdaAmountOutput")
  @Mapping(target = "amount.currency.symbol", constant = Constants.ADA)
  @Mapping(target = "amount.currency.decimals", constant = Constants.ADA_DECIMALS_STRING)
  @Mapping(target = "coinChange.coinIdentifier.identifier", source = "model", qualifiedByName = "getUtxoName")
  Operation toDto(Utxo model, OperationStatus status, int index);

}
