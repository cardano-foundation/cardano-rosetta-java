package org.cardanofoundation.rosetta.common.util;

import co.nstant.in.cbor.model.UnicodeString;
import org.apache.commons.lang3.ObjectUtils;

public class Formatters {

  public static final String EMPTY_HEX = "\\x";

  private Formatters() {
  }

  public static UnicodeString key(String key) {
    return new UnicodeString(key);
  }

  public static boolean isEmptyHexString(String toCheck) {
    if (ObjectUtils.isEmpty(toCheck)) {
      return true;
    }
    return toCheck.equals(EMPTY_HEX);
  }

  public static String remove0xPrefix(String hex) {
    if (hex.startsWith("0x")) {
      return hex.substring(2);
    }
    return hex;
  }

}
