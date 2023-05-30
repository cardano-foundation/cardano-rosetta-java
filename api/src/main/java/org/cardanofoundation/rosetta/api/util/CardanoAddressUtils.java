package org.cardanofoundation.rosetta.api.util;


import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.ByronAddress;
import java.util.Arrays;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.api.common.enumeration.StakeAddressPrefix;

public class CardanoAddressUtils {

  private CardanoAddressUtils() {

  }

  public static EraAddressType getEraAddressType(String address) {
    if (address == null) {
      return null;
    }

    if (address.startsWith("addr") || address.startsWith("stake")) { //Shelley address
      Address addressObj = new Address(address);
      return EraAddressType.SHELLEY;
    } else { //Try for byron address
      ByronAddress byronAddress = new ByronAddress(address);
      return EraAddressType.BYRON;
    }

  }

  public static boolean isStakeAddress(String address) {
    String addressPrefix = address.substring(0, Constants.PREFIX_LENGTH);
    String[] types = {StakeAddressPrefix.MAIN.getPrefix(), StakeAddressPrefix.TEST.getPrefix()};

    return Arrays.stream(types)
        .anyMatch(addressPrefix::contains);
  }
}
