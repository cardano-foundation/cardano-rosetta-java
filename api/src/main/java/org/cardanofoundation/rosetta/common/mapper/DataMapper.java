package org.cardanofoundation.rosetta.common.mapper;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.common.mapper.TokenRegistryMapper;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.CurrencyResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DataMapper {

  private final TokenRegistryMapper tokenRegistryMapper;

  /**
   * Basic mapping if a value is spent or not.
   *
   * @param value value to be mapped
   * @param spent if the value is spent. Will add a "-" in front of the value if spent.
   * @return the mapped value
   */
  public String mapValue(String value, boolean spent) {
    return spent ? "-" + value : value;
  }

  /**
   * Creates a Rosetta compatible Amount. Symbol and decimals are optional. If not provided, ADA and
   * 6 decimals are used.
   *
   * @param value    The amount of the token
   * @param symbol   The symbol of the token - it will be hex encoded (null for ADA)
   * @param decimals The number of decimals of the token (null for ADA)
   * @param metadata The metadata of the token (domain object, null for ADA)
   * @return The Rosetta compatible Amount
   */
  public Amount mapAmount(String value,
                          @Nullable String symbol,
                          @Nullable Integer decimals,
                          @Nullable TokenRegistryCurrencyData metadata) {
    if (Objects.isNull(symbol)) {
      symbol = Constants.ADA;
      decimals = Constants.ADA_DECIMALS;
    }
    Amount amount = new Amount();
    amount.setValue(value);

    // Convert domain metadata to response metadata (without decimals field) for serialization
    CurrencyMetadataResponse metadataResponse = metadata != null ? tokenRegistryMapper.toCurrencyMetadataResponse(metadata) : null;

    amount.setCurrency(CurrencyResponse.builder()
        .symbol(symbol)
        .decimals(decimals)
        .metadata(metadataResponse)
        .build()
    );

    return amount;
  }

}
