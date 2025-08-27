package org.cardanofoundation.rosetta.common.mapper;

import java.util.Objects;

import lombok.RequiredArgsConstructor;

import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;

import org.cardanofoundation.rosetta.common.util.Constants;

@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DataMapper {

  /**
   * Basic mapping if a value is spent or not.
   *
   * @param value value to be mapped
   * @param spent if the value is spent. Will add a "-" in front of the value if spent.
   * @return the mapped value
   */
  public static String mapValue(String value, boolean spent) {
    return spent ? "-" + value : value;
  }

  /**
   * Creates a Rosetta compatible Amount. Symbol and decimals are optional. If not provided, ADA and
   * 6 decimals are used.
   *
   * @param value    The amount of the token
   * @param symbol   The symbol of the token - it will be hex encoded
   * @param decimals The number of decimals of the token
   * @param metadata The metadata of the token
   * @return The Rosetta compatible Amount
   */
  public static Amount mapAmount(String value, String symbol, Integer decimals,
      CurrencyMetadata metadata) {
    if (Objects.isNull(symbol)) {
      symbol = Constants.ADA;
    }
    if (Objects.isNull(decimals)) {
      decimals = Constants.ADA_DECIMALS;
    }
    Amount amount = new Amount();
    amount.setValue(value);
    amount.setCurrency(Currency.builder()
        .symbol(symbol)
        .decimals(decimals)
        .metadata(metadata)
        .build());
    return amount;
  }

}
