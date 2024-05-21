package org.cardanofoundation.rosetta.common.util;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.bloxbean.cardano.client.address.Address;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.enumeration.CatalystDataIndexes;
import org.cardanofoundation.rosetta.common.enumeration.CatalystSigIndexes;

import static org.cardanofoundation.rosetta.common.exception.ExceptionFactory.invalidPolicyIdError;
import static org.cardanofoundation.rosetta.common.exception.ExceptionFactory.invalidTokenNameError;
import static org.cardanofoundation.rosetta.common.util.Formatters.hexStringToBuffer;
import static org.cardanofoundation.rosetta.common.util.Formatters.isEmptyHexString;


public class ValidationUtil {

  private static final Pattern TOKEN_NAME_VALIDATION = Pattern.compile(
      "^[0-9a-fA-F]{0," + Constants.ASSET_NAME_LENGTH + "}$");
  private static final Pattern POLICY_ID_VALIDATION = Pattern.compile(
      "^[0-9a-fA-F]{" + Constants.POLICY_ID_LENGTH + "}$");

  private ValidationUtil() {

  }

  public static void validateCurrencies(List<Currency> currencies) {
    for (Currency currency : currencies) {
      String symbol = currency.getSymbol();
      CurrencyMetadata metadata = currency.getMetadata();
      if (!isTokenNameValid(symbol)) {
        throw invalidTokenNameError("Given name is " + symbol);
      }
      if (!symbol.equals(Constants.ADA)
          && (metadata == null || !isPolicyIdValid(String.valueOf(metadata.getPolicyId())))) {
        String policyId = metadata == null ? null : metadata.getPolicyId();
        throw invalidPolicyIdError("Given policy id is " + policyId);
      }
    }
  }

  public static boolean isTokenNameValid(String name) {
    return TOKEN_NAME_VALIDATION.matcher(name).matches() || isEmptyHexString(name);
  }

  public static boolean isPolicyIdValid(String policyId) {
    return POLICY_ID_VALIDATION.matcher(policyId).matches();
  }

  public static List<Currency> filterRequestedCurrencies(List<Currency> currencies) {
    if (currencies != null && currencies.stream().map(Currency::getSymbol)
        .noneMatch(Constants.ADA::equals)) {
      return currencies;
    } else {
      return Collections.emptyList();
    }
  }

  public static Address getAddressFromHexString(String hex) {
    try {
      return new Address(hexStringToBuffer(hex));
    } catch (Exception e) {
      return null;
    }
  }

  // [FutureUse] This method will be used to validate Transaction votes.
  public static boolean validateVoteDataFields(Map<String, Object> object) {
    List<CatalystDataIndexes> hexStringIndexes = Arrays.asList(
        CatalystDataIndexes.REWARD_ADDRESS,
        CatalystDataIndexes.STAKE_KEY,
        CatalystDataIndexes.VOTING_KEY
    );
    boolean isValidVotingNonce =
        object.containsKey(CatalystDataIndexes.VOTING_NONCE.getValue().toString())
            && object.get(CatalystDataIndexes.VOTING_NONCE.getValue().toString()) instanceof Number;

    return isValidVotingNonce
        && hexStringIndexes.stream().allMatch(index ->
        object.containsKey(index.getValue().toString()) && isHexString(
            object.get(index.getValue().toString()).toString()));
  }

  // [FutureUse] Votes-related code.
  public static boolean isVoteSignatureValid(Map<String, Object> mapJsonString) {

    List<Integer> dataIndexes = Arrays.stream(CatalystSigIndexes.values())
        .map(CatalystSigIndexes::getValue)
        .filter(value -> value > 0)
        .toList();
    return dataIndexes.stream().allMatch(index ->
        mapJsonString.containsKey(String.valueOf(index))
            && isHexString(mapJsonString.get(String.valueOf(index))));
  }

  // [FutureUse] Votes-related code.
  public static boolean isVoteDataValid(Map<String, Object> jsonObject) {
    boolean isObject = Objects.nonNull(jsonObject);

    return isObject && validateVoteDataFields(jsonObject);

  }

  public static boolean isHexString(Object value) {
    if (value instanceof String str) {
      return str.matches("^(0x)?[0-9a-fA-F]+$");
    }
    return false;
  }

  // [FutureUse]
  public static boolean areEqualUtxos(Utxo firstUtxo, Utxo secondUtxo) {
    return Objects.equals(firstUtxo.getOutputIndex(), secondUtxo.getOutputIndex())
        && Objects.equals(firstUtxo.getTxHash(), secondUtxo.getTxHash());
  }

}
