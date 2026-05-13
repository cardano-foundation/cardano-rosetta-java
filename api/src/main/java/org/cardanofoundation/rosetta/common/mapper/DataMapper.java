package org.cardanofoundation.rosetta.common.mapper;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.common.mapper.TokenRegistryMapper;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.CurrencyMetadataResponse;
import org.openapitools.client.model.CurrencyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DataMapper {

  private final TokenRegistryMapper tokenRegistryMapper;

  /**
   * Controls whether token-registry enrichment fields ({@code subject}, {@code name},
   * {@code description}, {@code ticker}, {@code url}, {@code logo}, {@code version}) are
   * serialized into {@code Currency.metadata}. When {@code false} (the default), only
   * {@code policyId} is exposed.
   * <p>
   * This flag does <em>not</em> affect {@code Currency.decimals} — decimals is a mandatory
   * cross-chain Rosetta field and always reflects the value resolved by {@code
   * TokenQueryService} (defaulting to {@code 0} when no CIP-26/CIP-68 data exists for the
   * token).
   */
  @Value("${cardano.rosetta.TOKEN_REGISTRY_ENABLED:false}")
  private boolean tokenRegistryEnabled;

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

    amount.setCurrency(CurrencyResponse.builder()
        .symbol(symbol)
        .decimals(decimals)
        .metadata(toMetadataResponse(metadata))
        .build()
    );

    return amount;
  }

  /**
   * Projects the domain-layer metadata onto the serialization-layer response. When token
   * registry enrichment is enabled, emits the full set of known fields; otherwise emits
   * only {@code policyId} so consumers can still identify the native asset by its minting
   * policy without leaking registry-sourced enrichment.
   */
  @Nullable
  private CurrencyMetadataResponse toMetadataResponse(@Nullable TokenRegistryCurrencyData metadata) {
    if (metadata == null) {
      return null;
    }
    if (tokenRegistryEnabled) {
      return tokenRegistryMapper.toCurrencyMetadataResponse(metadata);
    }
    return CurrencyMetadataResponse.builder()
        .policyId(metadata.getPolicyId())
        .build();
  }

}
