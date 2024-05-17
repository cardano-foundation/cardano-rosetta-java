package org.cardanofoundation.rosetta.common.mapper;

import java.math.BigInteger;
import java.util.Optional;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationStatus;

import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.util.Constants;

@Mapper(config = BaseMapper.class)
public interface UtxoMapper {

  @Mapping(target = "status", source = "status.status")
  @Mapping(target = "operationIdentifier.index", expression  = "java((long)index)")
  @Mapping(target = "account.address", source = "utxo.ownerAddr")
  @Mapping(target = "amount.value", source = "utxo", qualifiedByName = "getAdaAmount")
  @Mapping(target = "amount.currency.symbol", expression = "java(org.cardanofoundation.rosetta.common.util.Constants.ADA)")
  @Mapping(target = "amount.currency.decimals", expression = "java(org.cardanofoundation.rosetta.common.util.Constants.ADA_DECIMALS)")
  @Mapping(target = "coinChange.coinIdentifier.identifier", expression = "java(utxo.getTxHash() + \":\" + utxo.getOutputIndex())")
  Operation mapToOperation(Utxo utxo, OperationStatus status, @Context int index, @Context boolean input);

  @Named("getAdaAmount")
  default String getAdaAmount(Utxo utxo, @Context boolean input) {
    BigInteger adaAmount = Optional.ofNullable(utxo.getAmounts())
        .map(amts -> amts.stream()
            .filter(amt -> amt.getAssetName().equals(Constants.LOVELACE))
            .findFirst()
            .map(Amt::getQuantity)
            .orElse(BigInteger.ZERO))
        .orElse(BigInteger.ZERO);
    return input ? adaAmount.negate().toString() : adaAmount.toString();
  }
}
