package org.cardanofoundation.rosetta.api.account.mapper;

import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.client.TokenRegistryHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.TokenMetadata;
import org.cardanofoundation.rosetta.client.model.domain.TokenPropertyNumber;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.mapstruct.Named;
import org.openapitools.client.model.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.bloxbean.cardano.client.util.HexUtil.encodeHexString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cardanofoundation.rosetta.common.util.Constants.MULTI_ASSET_DECIMALS;

@Component
@RequiredArgsConstructor
public class AccountMapperUtil {

  private final TokenRegistryHttpGateway tokenRegistryHttpGateway;

  @Named("mapAddressBalancesToAmounts")
  public List<Amount> mapAddressBalancesToAmounts(List<AddressBalance> balances) {
    BigInteger lovelaceAmount = balances.stream()
        .filter(b -> Constants.LOVELACE.equals(b.unit()))
        .map(AddressBalance::quantity)
        .findFirst()
        .orElse(BigInteger.ZERO);

    List<Amount> amounts = new ArrayList<>();
    // always adding lovelace amount to the beginning of the list. Even if lovelace amount is 0
    amounts.add(DataMapper.mapAmount(String.valueOf(lovelaceAmount), null, null, null));

    // Filter native token balances (those with proper unit format)
    List<AddressBalance> nativeTokenBalances = balances.stream()
        .filter(b -> !Constants.LOVELACE.equals(b.unit()))
        .filter(b -> b.unit().length() >= Constants.POLICY_ID_LENGTH) // Has policyId + assetName (assetName can be empty)
        .toList();

    if (nativeTokenBalances.isEmpty()) {
      return amounts;
    }

    // Collect all subjects (policyId + assetName concatenated) for batch fetching
    Set<String> subjects = nativeTokenBalances.stream()
        .map(b -> {
          String policyId = b.unit().substring(0, Constants.POLICY_ID_LENGTH);
          String assetName = b.unit().substring(Constants.POLICY_ID_LENGTH);
          String subject = "%s%s".formatted(policyId, encodeHexString(assetName.getBytes(UTF_8)));

          return subject;
        })
        .collect(Collectors.toSet());

    // Fetch token metadata for all subjects in batch
    Map<String, Optional<TokenSubject>> tokenMetadataMap = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

    // Process each native token balance with metadata
      for (AddressBalance b : nativeTokenBalances) {
          String symbol = b.unit().substring(Constants.POLICY_ID_LENGTH);
          String policyId = b.unit().substring(0, Constants.POLICY_ID_LENGTH);
          String subject = "%s%s".formatted(policyId, encodeHexString(symbol.getBytes(UTF_8)));

          // Get metadata if available
          Optional<TokenSubject> tokenMetadata = tokenMetadataMap.getOrDefault(subject, Optional.empty());

          CurrencyMetadataResponse metadata = extractTokenMetadata(policyId, tokenMetadata);
          int decimals = extractTokenDecimals(tokenMetadata);

          amounts.add(
                  DataMapper.mapAmount(b.quantity().toString(),
                          symbol,
                          decimals,
                          metadata)
          );
      }

      return amounts;
  }

  @Named("mapUtxosToCoins")
  public List<Coin> mapUtxosToCoins(List<Utxo> utxos) {
    return utxos.stream().map(utxo -> {
      Amt adaAsset = utxo.getAmounts().stream()
          .filter(amt -> Constants.LOVELACE.equals(amt.getUnit()))
          .findFirst()
          .orElseGet(() -> new Amt(null, null, Constants.ADA, BigInteger.ZERO));
      String coinIdentifier = utxo.getTxHash() + ":" + utxo.getOutputIndex();

      return Coin.builder()
          .coinIdentifier(new CoinIdentifier(coinIdentifier))
          .amount(Amount.builder()
              .value(adaAsset.getQuantity().toString())
              .currency(getAdaCurrency())
              .build())

          .metadata(mapCoinMetadata(utxo, coinIdentifier))
          .build();
    }).toList();
  }

  @Nullable
  private Map<String, List<CoinTokens>> mapCoinMetadata(Utxo utxo, String coinIdentifier) {
    // Filter only native tokens (non-ADA amounts with policyId)
    List<Amt> nativeTokenAmounts = utxo.getAmounts().stream()
        .filter(Objects::nonNull)
        .filter(amount -> amount.getPolicyId() != null
            && amount.getAssetName() != null // assetName can be empty string for tokens with no name
            && amount.getQuantity() != null)
        .filter(amount -> !Constants.LOVELACE.equals(amount.getAssetName())) // exclude ADA
        .toList();

    if (nativeTokenAmounts.isEmpty()) {
      return null;
    }

    // Collect all subjects (policyId + assetName concatenated) for batch fetching
    Set<String> subjects = nativeTokenAmounts.stream()
        .map(amount -> amount.getPolicyId() + encodeHexString(amount.getAssetName().getBytes(UTF_8)))
        .collect(Collectors.toSet());

    // Fetch token metadata for all subjects in batch
    Map<String, Optional<TokenSubject>> tokenMetadataMap = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

    // Create separate CoinTokens entry for each native token (one token per entry)
    List<CoinTokens> coinTokens = nativeTokenAmounts.stream()
        .map(amount -> {
          String policyId = amount.getPolicyId();
          String subject = policyId + encodeHexString(amount.getAssetName().getBytes(UTF_8));
          Optional<TokenSubject> tokenMetadata = tokenMetadataMap.getOrDefault(subject, Optional.empty());
          
          CurrencyMetadataResponse metadata = extractTokenMetadata(policyId, tokenMetadata);
          int decimals = extractTokenDecimals(tokenMetadata);
          
          Amount tokenAmount = DataMapper.mapAmount(amount.getQuantity().toString(),
              // unit = assetName + policyId. To get the symbol policy ID must be removed from Unit. According to CIP67
              amount.getUnit().replace(amount.getPolicyId(), ""),
              decimals, metadata);
          
          CoinTokens tokens = new CoinTokens();
          tokens.setPolicyId(policyId);
          tokens.setTokens(List.of(tokenAmount));
          return tokens;
        })
        .toList();

    return coinTokens.isEmpty() ? null : Map.of(coinIdentifier, coinTokens);
  }

  private CurrencyResponse getAdaCurrency() {
    return CurrencyResponse.builder()
        .symbol(Constants.ADA)
        .decimals(Constants.ADA_DECIMALS)
        .build();
  }

  private static int extractTokenDecimals(Optional<TokenSubject> tokenMetadata) {
    return tokenMetadata
        .map(TokenSubject::getMetadata)
        .map(TokenMetadata::getDecimals)
        .map(TokenPropertyNumber::getValue)
        .map(Long::intValue)
        .orElse(MULTI_ASSET_DECIMALS);
  }

  private static CurrencyMetadataResponse extractTokenMetadata(String policyId,
                                                               Optional<TokenSubject> tokenMetadata) {
    CurrencyMetadataResponse.CurrencyMetadataResponseBuilder builder = CurrencyMetadataResponse.builder()
        .policyId(policyId);

    tokenMetadata.ifPresent(t -> {
      TokenMetadata tokenMeta = t.getMetadata();

      // Mandatory fields from registry API
      builder.subject(t.getSubject());
      builder.name(tokenMeta.getName().getValue());
      builder.description(tokenMeta.getDescription().getValue());

      // Optional fields
      Optional.ofNullable(tokenMeta.getTicker()).ifPresent(ticker -> builder.ticker(ticker.getValue()));
      Optional.ofNullable(tokenMeta.getUrl()).ifPresent(url -> builder.url(url.getValue()));
      Optional.ofNullable(tokenMeta.getLogo()).ifPresent(logo -> builder.logo(logo.getValue()));
      Optional.ofNullable(tokenMeta.getVersion()).ifPresent(version -> builder.version(BigDecimal.valueOf(version.getValue())));
    });

    return builder.build();
  }
}
