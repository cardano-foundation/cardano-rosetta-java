package org.cardanofoundation.rosetta.api.util;


import static org.cardanofoundation.rosetta.api.exception.ExceptionFactory.invalidPolicyIdError;
import static org.cardanofoundation.rosetta.api.exception.ExceptionFactory.invalidTokenNameError;
import static org.cardanofoundation.rosetta.api.util.Formatters.hexStringToBuffer;
import static org.cardanofoundation.rosetta.api.util.Formatters.isEmptyHexString;

import com.bloxbean.cardano.client.address.Address;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystDataIndexes;
import org.cardanofoundation.rosetta.api.common.enumeration.CatalystSigIndexes;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;


public class Validations {

  private static final Pattern TOKEN_NAME_VALIDATION = Pattern.compile(
      "^[0-9a-fA-F]{0," + Constants.ASSET_NAME_LENGTH + "}$");
  private static final Pattern POLICY_ID_VALIDATION = Pattern.compile(
      "^[0-9a-fA-F]{" + Constants.POLICY_ID_LENGTH + "}$");

  private Validations() {

  }

  public static void validateCurrencies(List<Currency> currencies) {
    for (Currency currency : currencies) {
      String symbol = currency.getSymbol();
      Map<String, Object> metadata = currency.getMetadata();
      if (!isTokenNameValid(symbol)) {
        throw invalidTokenNameError("Given name is " + symbol);
      }
      if (
          !symbol.equals(Constants.ADA)
              && !isPolicyIdValid(String.valueOf(metadata.get("policyId")))) {
        throw invalidPolicyIdError("Given policy id is " + metadata.get("policyId"));
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

  public static boolean isVoteSignatureValid(Map<String, Object> mapJsonString) {

    List<Integer> dataIndexes = Arrays.stream(CatalystSigIndexes.values())
        .map(CatalystSigIndexes::getValue)
        .filter(value -> value > 0)
        .toList();
    return dataIndexes.stream().allMatch(index ->
        mapJsonString.containsKey(String.valueOf(index))
            && isHexString(mapJsonString.get(String.valueOf(index))));
  }

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


  public static boolean areEqualUtxos(Utxo firstUtxo, Utxo secondUtxo) {
    return Objects.equals(firstUtxo.getIndex(), secondUtxo.getIndex())
        && Objects.equals(firstUtxo.getTransactionHash(), secondUtxo.getTransactionHash());
  }

}
