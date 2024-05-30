package org.cardanofoundation.rosetta.common.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.ConstructionMetadataResponse;
import org.openapitools.client.model.ConstructionMetadataResponseMetadata;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;
import org.openapitools.client.model.Signature;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.common.annotation.PersistenceMapper;
import org.cardanofoundation.rosetta.common.model.cardano.crypto.Signatures;
import org.cardanofoundation.rosetta.common.util.Constants;

@Slf4j
@PersistenceMapper
@RequiredArgsConstructor
public class DataMapper {

  private final ProtocolParamsToRosettaProtocolParameters protocolParamsToRosettaProtocolParameters;

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

  public ConstructionMetadataResponse mapToMetadataResponse(ProtocolParams protocolParams, Long ttl,
      Long suggestedFee) {
    return ConstructionMetadataResponse.builder()
        .metadata(ConstructionMetadataResponseMetadata.builder()
            .ttl(new BigDecimal(ttl))
            .protocolParameters(
                protocolParamsToRosettaProtocolParameters.toProtocolParameters(protocolParams))
            .build())
        .suggestedFee(List.of(Amount.builder()
            .value(suggestedFee.toString())
            .currency(getAdaCurrency())
            .build()))
        .build();
  }

  public static List<Signatures> mapRosettaSignatureToSignaturesList(List<Signature> signatures) {
    return signatures.stream().map(signature -> {
      String chainCode = null;
      String address = null;
      AccountIdentifier accountIdentifier = signature.getSigningPayload().getAccountIdentifier();
      if (!ObjectUtils.isEmpty(accountIdentifier)) {
        chainCode = accountIdentifier.getMetadata().getChainCode();
        address = accountIdentifier.getAddress();
      }
      return new Signatures(signature.getHexBytes(), signature.getPublicKey().getHexBytes(),
          chainCode, address);
    }).toList();
  }

  private static Currency getAdaCurrency() {
    return Currency.builder()
        .symbol(Constants.ADA)
        .decimals(Constants.ADA_DECIMALS)
        .build();
  }
}
