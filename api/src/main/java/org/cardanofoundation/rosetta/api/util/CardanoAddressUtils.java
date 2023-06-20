package org.cardanofoundation.rosetta.api.util;


import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressType;
import com.bloxbean.cardano.client.address.util.AddressEncoderDecoderUtil;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.exception.AddressRuntimeException;
import com.bloxbean.cardano.client.transaction.spec.NetworkId;
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
  public static Address getAddress(byte[] paymentKeyHash, byte[] stakeKeyHash, byte headerKind, Network networkInfo, AddressType addressType) {
    NetworkId network = AddressEncoderDecoderUtil.getNetworkId(networkInfo);
    String var10000 = AddressEncoderDecoderUtil.getPrefixHeader(addressType);
    String prefix = var10000 + AddressEncoderDecoderUtil.getPrefixTail(network);
    byte header = AddressEncoderDecoderUtil.getAddressHeader(headerKind, networkInfo, addressType);
    byte[] addressArray = getAddressBytes(paymentKeyHash, stakeKeyHash, addressType, header);
    return new Address(prefix, addressArray);
  }
  private static byte[] getAddressBytes(byte[] paymentKeyHash, byte[] stakeKeyHash, AddressType addressType, byte header) {
    byte[] addressArray;
    switch (addressType) {
      case Base:
        addressArray = new byte[1 + paymentKeyHash.length + stakeKeyHash.length];
        addressArray[0] = header;
        System.arraycopy(paymentKeyHash, 0, addressArray, 1, paymentKeyHash.length);
        System.arraycopy(stakeKeyHash, 0, addressArray, paymentKeyHash.length + 1, stakeKeyHash.length);
        break;
      case Enterprise:
        addressArray = new byte[1 + paymentKeyHash.length];
        addressArray[0] = header;
        System.arraycopy(paymentKeyHash, 0, addressArray, 1, paymentKeyHash.length);
        break;
      case Reward:
        addressArray = new byte[1 + stakeKeyHash.length];
        addressArray[0] = header;
        System.arraycopy(stakeKeyHash, 0, addressArray, 1, stakeKeyHash.length);
        break;
      case Ptr:
        addressArray = new byte[1 + paymentKeyHash.length + stakeKeyHash.length];
        addressArray[0] = header;
        System.arraycopy(paymentKeyHash, 0, addressArray, 1, paymentKeyHash.length);
        System.arraycopy(stakeKeyHash, 0, addressArray, paymentKeyHash.length + 1, stakeKeyHash.length);
        break;
      default:
        throw new AddressRuntimeException("Unknown address type");
    }

    return addressArray;
  }
}
