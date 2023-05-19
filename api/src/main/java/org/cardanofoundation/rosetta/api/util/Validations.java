package org.cardanofoundation.rosetta.api.util;


import static org.cardanofoundation.rosetta.api.util.Formatters.isEmptyHexString;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.model.rest.Currency;


public class Validations {

  private static final Pattern tokenNameValidation = Pattern.compile(
      "^[0-9a-fA-F]{0," + Constants.ASSET_NAME_LENGTH + "}$");
  private static final Pattern policyIdValidation = Pattern.compile(
      "^[0-9a-fA-F]{" + Constants.POLICY_ID_LENGTH + "}$");

  private Validations() {

  }

  public static void validateCurrencies(List<Currency> currencies) {
    for (Currency currency : currencies) {
      String symbol = currency.getSymbol();
      Map<String, Object> metadata = currency.getMetadata();
      if (!isTokenNameValid(symbol)) {
        throw new RuntimeException("Given name is " + symbol);
      }
      if (!symbol.equals(Constants.ADA) && !isPolicyIdValid(
          (String) metadata.get("policyId"))) {
        throw new RuntimeException("Given policy id is " + metadata.get("policyId"));
      }
    }
  }

  public static boolean isTokenNameValid(String name) {
    return tokenNameValidation.matcher(name).matches() || isEmptyHexString(name);
  }

  public static boolean isPolicyIdValid(String policyId) {
    return policyIdValidation.matcher(policyId).matches();
  }

  public static List<Currency> filterRequestedCurrencies(List<Currency> currencies) {
    if (currencies != null && currencies.stream().map(Currency::getSymbol)
        .noneMatch(Constants.ADA::equals)) {
      return currencies;
    } else {
      return Collections.emptyList();
    }
  }


}
