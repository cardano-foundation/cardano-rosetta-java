package org.cardanofoundation.rosetta.api.block.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.mutable.MutableInt;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.OperationMetadata;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.TokenBundleItem;

import org.cardanofoundation.rosetta.api.account.model.entity.Amt;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.util.Constants;

public abstract class  AbstractToOperation<T> {

  abstract Operation toDto(T model, OperationStatus status, int index);

  protected List<Operation> convert(List<T> stakeReg, OperationStatus status) {
    MutableInt mutableIndexHolder = new MutableInt(0);
    return Optional.ofNullable(stakeReg)
        .stream()
        .flatMap(List::stream)
        .map(t -> toDto(t, status, mutableIndexHolder.incrementAndGet()))
        .collect(Collectors.toList());
  }

  protected static  OperationMetadata mapToOperationMetaData(boolean spent, List<Amt> amounts) {
    OperationMetadata operationMetadata = new OperationMetadata();
    for (Amt amount : amounts) {
      if (!amount.getAssetName().equals(Constants.LOVELACE)) {
        TokenBundleItem tokenBundleItem = new TokenBundleItem();
        tokenBundleItem.setPolicyId(amount.getPolicyId());
        Amount amt = new Amount();
        amt.setValue(DataMapper.mapValue(amount.getQuantity().toString(), spent));
        String hexAssetName = Hex.encodeHexString(amount.getAssetName().getBytes());
        amt.setCurrency(Currency.builder()
            .symbol(hexAssetName)
            .decimals(0)
            .build());
        tokenBundleItem.setTokens(List.of(amt));
        operationMetadata.addTokenBundleItem(tokenBundleItem);
      }
    }
    return operationMetadata;
  }

}
