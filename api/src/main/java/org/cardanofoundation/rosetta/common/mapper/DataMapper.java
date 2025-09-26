package org.cardanofoundation.rosetta.common.mapper;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.CurrencyResponse;

import javax.annotation.Nullable;
import java.util.Objects;

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
  public static Amount mapAmount(String value,
                                 String symbol,
                                 Integer decimals,
                                 @Nullable CurrencyMetadataResponse metadata) {
    if (Objects.isNull(symbol)) {
      symbol = Constants.ADA;
    }
    if (Objects.isNull(decimals)) {
      decimals = Constants.ADA_DECIMALS;
    }
    Amount amount = new Amount();
    amount.setValue(value);
    amount.setCurrency(CurrencyResponse.builder()
        .symbol(symbol)
        .decimals(decimals)
        .metadata(metadata)
        .build()
    );

    return amount;
  }

}
