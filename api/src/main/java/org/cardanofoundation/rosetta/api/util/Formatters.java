package org.cardanofoundation.rosetta.api.util;

import com.bloxbean.cardano.client.util.HexUtil;
import org.apache.commons.lang3.ObjectUtils;

public class Formatters {

  public static final String EMPTY_HEX = "\\x";

  private Formatters() {
  }

  public static String removePrefixHex(String hexString) {
    if(hexString != null && hexString.startsWith("0x"))
      hexString = hexString.substring(2);
    return hexString;
  }

  public static byte[] hexStringToBuffer(String input) {
    return HexUtil.decodeHexString(isEmptyHexString(input) ? "" : input);
  }

  public static boolean isEmptyHexString(String toCheck) {
    if (ObjectUtils.isEmpty(toCheck)) {
      return true;
    }
    return toCheck.equals(EMPTY_HEX);
  }

  public static String hexStringFormatter(String toFormat) {
    return toFormat == null || toFormat.isEmpty() ? EMPTY_HEX : toFormat;
  }
  public static String remove0xPrefix(String hex) {
    if (hex.startsWith("0x")) {
      return hex.substring(2);
    }
    return hex;
  }

}
