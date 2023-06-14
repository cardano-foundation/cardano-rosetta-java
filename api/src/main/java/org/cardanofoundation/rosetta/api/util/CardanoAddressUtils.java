package org.cardanofoundation.rosetta.api.util;


import java.util.Arrays;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.StakeAddressPrefix;

public class CardanoAddressUtils {

  private CardanoAddressUtils() {

  }

  public static boolean isStakeAddress(String address) {
    String addressPrefix = address.substring(0, Constants.PREFIX_LENGTH);
    String[] types = {StakeAddressPrefix.MAIN.getPrefix(), StakeAddressPrefix.TEST.getPrefix()};

    return Arrays.stream(types)
        .anyMatch(addressPrefix::contains);
  }
}
